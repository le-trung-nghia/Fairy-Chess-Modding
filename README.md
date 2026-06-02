# COMP1020 Final Project Team 8: Fairy Chess Example Piece Pack

This repository serves as example of a piece pack for our Fairy Chess project.
This repository can be found on GitHub at <https://github.com/le-trung-nghia/Fairy-Chess-Modding>.

## Dependencies

The `chessboard-logic` artifact from the Fairy Chess project is a required dependency.
The `StreamEx` library is useful to manipulate the iterators of positions returned by `BoardRegion`s, though not required.

## Pack Header

Fairy Chess reads the custom field `Pack-Name` in the `.jar` manifest file to get the name of the piece pack. Therefore, this field is set using the `maven-jar-plugin`

Fairy Chess loads the piece classes within a pack by using the Service Provider Interface. Piece classes must be defined in the `src/main/resources/META-INF/services/com.chess.logic.types.Piece` file by listing the fully qualified paths of the classes, one on each line.

## Piece Creation

Each piece corresponds to a class in the piece pack.

Extend the `Piece` abstract class in `chessboard-logic` to create a piece. The essential methods of the piece are all abstract and therefore must be implemented. A piece can also override the non-abstract methods to listen to events involving itself.

## Piece Resources

A piece may use its own image resources. These should be put in the `src/main/resources` folder.

## Building

Run `mvn clean package` to build the piece pack. The resulting `.jar` file is in the `target` folder.
