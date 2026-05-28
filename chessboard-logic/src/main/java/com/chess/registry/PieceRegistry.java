package com.chess.registry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.chess.logic.types.Piece;

public class PieceRegistry {
    private final Map<String, Map<String, Constructor<? extends Piece>>> registry = new HashMap<>();

    // Loaders are kept open for the registry's lifetime so that
    // piece classes can still call getResourceAsStream() on their own JAR.
    private final List<URLClassLoader> openLoaders = new ArrayList<>();

    public void registerPiecePack(File pack) {
        // Do NOT use try-with-resources here — closing the loader would seal
        // the JAR and make all future getResourceAsStream() calls return null.
        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(
                    new URL[]{ pack.toURI().toURL() },
                    this.getClass().getClassLoader());

            URL manifestUrl = loader.findResource("META-INF/MANIFEST.MF");
            if (manifestUrl == null) {
                throw new IllegalArgumentException(
                        "Pack file %s does not have a manifest".formatted(pack));
            }

            String packName;
            try (InputStream is = manifestUrl.openStream()) {
                Manifest manifest = new Manifest(is);
                Attributes attrs = manifest.getMainAttributes();
                packName = attrs.getValue("Pack-Name");
            }
            if (packName == null || packName.isBlank()) {
                throw new IllegalArgumentException(
                        "Pack file %s does not have a Pack-Name entry".formatted(pack));
            }
            if (registry.containsKey(packName)) {
                throw new IllegalArgumentException(
                        "Pack file %s: pack name '%s' is already registered"
                                .formatted(pack, packName));
            }

            registry.put(packName, new HashMap<>());

            ServiceLoader<Piece> serviceLoader = ServiceLoader.load(Piece.class, loader);
            for (var provider : (Iterable<ServiceLoader.Provider<Piece>>)
                    () -> serviceLoader.stream().iterator()) {
                Constructor<? extends Piece> ctor = provider.type().getDeclaredConstructor();
                String pieceName = ctor.newInstance().identifier();
                registry.get(packName).put(pieceName, ctor);
            }

            // Only keep the loader alive after everything succeeded
            openLoaders.add(loader);

        } catch (IOException | InstantiationException | InvocationTargetException
                | IllegalAccessException | NoSuchMethodException e) {
            if (loader != null) try { loader.close(); } catch (IOException ignored) {}
            throw new RuntimeException(
                    "Error loading pack file %s: %s".formatted(pack, e));
        } catch (IllegalArgumentException e) {
            if (loader != null) try { loader.close(); } catch (IOException ignored) {}
            throw new RuntimeException(e.getMessage());
        }
    }

    /** Returns pack → [pieceNames] for every registered pack, in insertion order. */
    public Map<String, List<String>> listRegisteredPieces() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (var entry : registry.entrySet()) {
            result.put(entry.getKey(),
                    Collections.unmodifiableList(new ArrayList<>(entry.getValue().keySet())));
        }
        return result;
    }

    public Piece instantiatePiece(PiecePath path) {
        if (!registry.containsKey(path.packName())) {
            throw new NoSuchElementException(
                    "Pack %s is not registered in the piece registry".formatted(path.packName()));
        }
        Map<String, Constructor<? extends Piece>> pack = registry.get(path.packName());
        if (!pack.containsKey(path.pieceName())) {
            throw new NoSuchElementException(
                    "Piece %s is not present in pack %s".formatted(path.pieceName(), path.packName()));
        }
        try {
            return pack.get(path.pieceName()).newInstance();
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError("Unexpected error: Instantiation of piece %s failed due to %s"
                    .formatted(path.toString(), e.toString()));
        }
    }
}
