package com.crestron.aurora.boardgames.chess;


import com.crestron.aurora.Loged;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public Piece[][] board;
    //public com.github.bhlangonijr.chesslib.Board boards;
    private int moveCount;
    private String lastMove;

    public Board() {
        //boards = new com.github.bhlangonijr.chesslib.Board();
        moveCount = 0;
        board = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new EmptySpace(false, new Location(Character.toString((char) ('A' + i)), j));
            }
        }

        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn(false, new Location(1, i));
            board[6][i] = new Pawn(true, new Location(6, i));
        }

        //white
        board[7][0] = new Rook(true, board[7][0].getLocation());
        board[7][1] = new Knight(true, board[7][1].getLocation());
        board[7][2] = new Bishop(true, board[7][2].getLocation());
        board[7][4] = new King(true, board[7][3].getLocation());
        board[7][3] = new Queen(true, board[7][4].getLocation());
        board[7][5] = new Bishop(true, board[7][5].getLocation());
        board[7][6] = new Knight(true, board[7][6].getLocation());
        board[7][7] = new Rook(true, board[7][7].getLocation());
        //black
        board[0][0] = new Rook(false, board[0][0].getLocation());
        board[0][1] = new Knight(false, board[0][1].getLocation());
        board[0][2] = new Bishop(false, board[0][2].getLocation());
        board[0][4] = new King(false, board[0][3].getLocation());
        board[0][3] = new Queen(false, board[0][4].getLocation());
        board[0][5] = new Bishop(false, board[0][5].getLocation());
        board[0][6] = new Knight(false, board[0][6].getLocation());
        board[0][7] = new Rook(false, board[0][7].getLocation());

        board[7][0] = new Rook(true, new Location(7, 0));
        board[7][1] = new Knight(true, new Location(7, 1));
        board[7][2] = new Bishop(true, new Location(7, 2));
        board[7][3] = new King(true, new Location(7, 3));
        board[7][4] = new Queen(true, new Location(7, 4));
        board[7][5] = new Bishop(true, new Location(7, 5));
        board[7][6] = new Knight(true, new Location(7, 6));
        board[7][7] = new Rook(true, new Location(7, 7));
        //black
        board[0][0] = new Rook(false, new Location(0, 0));
        board[0][1] = new Knight(false, new Location(0, 1));
        board[0][2] = new Bishop(false, new Location(0, 2));
        board[0][3] = new King(false, new Location(0, 3));
        board[0][4] = new Queen(false, new Location(0, 4));
        board[0][5] = new Bishop(false, new Location(0, 5));
        board[0][6] = new Knight(false, new Location(0, 6));
        board[0][7] = new Rook(false, new Location(0, 7));

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j].setBoard(this);
            }
        }

    }

    public String move(Location from, Location to, boolean turn) {
        String s = board[from.getNumRepLetter()][from.getNum()].getPieceValue();
        Location l = board[to.getNumRepLetter()][to.getNum()].getLocation();
        if (!isEmpty(to)) {
            s += "x";
        }
        board[to.getNumRepLetter()][to.getNum()] = board[from.getNumRepLetter()][from.getNum()];
        board[to.getNumRepLetter()][to.getNum()].setLocation(l);
        board[from.getNumRepLetter()][from.getNum()] = new EmptySpace(false, from);
        board[from.getNumRepLetter()][from.getNum()].setBoard(this);

        from.setNum(from.getNum() + 1);
        to.setNum(to.getNum() + 1);
        s += to.toString().toLowerCase();

        //UtilLog.i(new Move(Square.fromValue(from.toString()), Square.fromValue(to.toString())).toString());
        //boards.doMove(new Move(moved, boards.getSideToMove()));
        lastMove = s;
        //boards.loadFromFen(toFEN());

        if (turn)
            moveCount++;

        return s;
    }


    public boolean isEmpty(Location loc) {
        return board[loc.getNumRepLetter()][loc.getNum()] instanceof EmptySpace;
    }

    public Piece getLocation(int l, int n) {
        return board[l][n];
    }

    public Piece getLocation(Location location) {
        return board[location.getNumRepLetter()][location.getNum()];
    }

    public String toFEN() {
        String s = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board[i][j];
                if (!isEmpty(Location.Locate.toLocation(i, j))) {
                    String q;
                    if (p instanceof Pawn) {
                        q = p.getColor() ? "P" : "p";
                    } else {
                        q = p.getColor() ? p.getPieceValue().toUpperCase() : p.getPieceValue().toLowerCase();
                    }
                    s += q;
                } else {
                    s += "8";
                }
            }
            s += "/";
        }
        s = s.replace("//", "/");
        s = s.substring(0, s.length() - 1);

        String[] s1 = s.split("/");

        for (int q = 0; q < s1.length; q++) {
            String s2 = s1[q];
            String s3 = "";
            if (s2.contains("8")) {
                int count = 0;
                s3 = "";
                for (int i = 0; i <= s2.length(); i++) {
                    if (i == s2.length()) {
                        if (count > 0) {
                            String re = "";
                            for (int j = 0; j < count; j++) {
                                re += "8";
                            }
                            if (!re.equals(""))
                                s3 = s3.replace(re, "" + count);
                        }
                        count = 0;
                    } else if (s2.charAt(i) != '8' && count > 0) {
                        String re = "";
                        for (int j = 0; j < count; j++) {
                            re += "8";
                        }
                        if (!re.equals(""))
                            s3 = s2.replaceFirst(re, "" + count);
                        count = 0;
                    } else if (s2.charAt(i) == '8') {
                        count++;
                    }
                }
                if (s3.equals("")) {
                    s3 = "8";
                }
            } else {
                s3 = s1[q];
            }
            s1[q] = s3;
        }

        s = "";
        for (String s2 : s1) {
            s += s2 + "/";
        }

        s = s.substring(0, s.length() - 1) + " ";
        //colorName += boards.getSideToMove().value().substring(0, 1).toLowerCase() + " ";
        s+="w ";
        String castle = "";
        if (!getLocation(0, 0).getMoved() && !getLocation(0, 4).getMoved()) {
            castle += "K";
        }

        if (!getLocation(0, 7).getMoved() && !getLocation(0, 4).getMoved()) {
            castle += "Q";
        }

        if (!getLocation(7, 0).getMoved() && !getLocation(7, 4).getMoved()) {
            castle += "k";
        }

        if (!getLocation(7, 7).getMoved() && !getLocation(7, 4).getMoved()) {
            castle += "q";
        }

        if (castle.isEmpty()) {
            s += "- ";
        } else {
            s += castle + " ";
        }

        if(lastMove==null) {
            s += "- ";
        } else {
            s += lastMove + " ";
        }

        s += moveCount;
        return s;
    }

    private List<String> numList = new ArrayList<String>() {{
        add("8");
        add("88");
        add("888");
        add("8888");
        add("88888");
        add("888888");
        add("8888888");
        add("88888888");
    }};

    public boolean fromFEN(String fen) {

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new EmptySpace(false, new Location(Character.toString((char) ('A' + i)), j));
            }
        }

        String[] s = fen.split("/");
        String[] other = s[7].substring(s[7].indexOf(" ") + 1).split(" ");
        moveCount = Integer.parseInt(other[other.length-1]);

        s[7] = s[7].substring(0, s[7].indexOf(" "));

        for (int i = 0; i < s.length; i++) {
            for(int j=0;j<s[i].length();j++) {
                if(!Character.isLetter(s[i].charAt(j)) && Character.getNumericValue(s[i].charAt(j))!=8) {
                    int num = Integer.parseInt(s[i].substring(j, j + 1));
                    s[i] = s[i].replace(""+num, numList.get(num-1));
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < s[i].length(); j++) {

                if (s[i].equals("8")) {
                    break;
                }

                Piece p;
                Location l = board[i][j].getLocation();

                switch (s[i].charAt(j)) {
                    case 'R':
                        p = new Rook(true, l);
                        if(!other[1].contains("K") || !other[1].contains("Q"))
                            p.setMoved(false);
                        break;
                    case 'r':
                        p = new Rook(false, l);
                        if(!other[1].contains("k") || !other[1].contains("q"))
                            p.setMoved(false);
                        break;
                    case 'K':
                        p = new King(true, l);
                        if(!other[1].contains("K") || !other[1].contains("Q"))
                            p.setMoved(false);
                        break;
                    case 'k':
                        p = new King(false, l);
                        if(!other[1].contains("k") || !other[1].contains("q"))
                            p.setMoved(false);
                        break;
                    case 'B':
                        p = new Bishop(true, l);
                        break;
                    case 'b':
                        p = new Bishop(false, l);
                        break;
                    case 'P':
                        p = new Pawn(true, l);
                        break;
                    case 'p':
                        p = new Pawn(false, l);
                        break;
                    case 'Q':
                        p = new Queen(true, l);
                        break;
                    case 'q':
                        p = new Queen(false, l);
                        break;
                    case 'N':
                        p = new Knight(true, l);
                        break;
                    case 'n':
                        p = new Knight(false, l);
                        break;
                    default:
                        p = new EmptySpace(false, l);
                        break;
                }

                p.setMoved(false);

                board[i][j] = p;

            }

        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j].setBoard(this);
            }
        }

        return other[1].equals("w");
    }

    public String toValues() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            s.append("|");
            for (int j = 0; j < 8; j++) {
                s.append(board[i][j].getValue()).append("|");
            }
            s.append("\n");
        }
        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            s.append("|");
            for (int j = 0; j < 8; j++) {
                s.append(board[i][j].getIcon()).append("|");
            }
            s.append("\n");
        }
        return s.toString();
    }

}
