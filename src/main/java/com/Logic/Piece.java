package com.Logic;

public class Piece {
    private String type;
    private String color;
    private int row;
    private int col;

    public Piece(String type, String color, int row, int col) {
        this.type = type;
        this.color = color;
        this.row = row;
        this.col = col;
    }
    
    public String getImageFileName() {
        return "piece_img/" + color.toLowerCase() + "_" + type.toLowerCase() + ".png";
    }
}