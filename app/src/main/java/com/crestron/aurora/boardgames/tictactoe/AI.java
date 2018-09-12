package com.crestron.aurora.boardgames.tictactoe;

public class AI {

    private static double maxPly;

    /**
     * MiniMax cannot be instantiated.
     */
    private AI() {
    }

    /**
     * Execute the algorithm.
     *
     * @param player the player that the AI will identify as
     * @param board  the Tic Tac Toe board to play on
     * @param maxPly the maximum depth
     */
    static void run(TTTBoard.State player, TTTBoard board, double maxPly) {
        if (maxPly < 1) {
            throw new IllegalArgumentException("Maximum depth must be greater than 0.");
        }

        AI.maxPly = maxPly;
        miniMax(player, board, 0);
    }

    /**
     * The meat of the algorithm.
     *
     * @param player     the player that the AI will identify as
     * @param board      the Tic Tac Toe board to play on
     * @param currentPly the current depth
     * @return the score of the board
     */
    private static int miniMax(TTTBoard.State player, TTTBoard board, int currentPly) {
        if (currentPly++ == maxPly || board.isGameOver()) {
            return score(player, board);
        }

        if (board.getTurn() == player) {
            return getMax(player, board, currentPly);
        } else {
            return getMin(player, board, currentPly);
        }

    }

    /**
     * Play the move with the highest score.
     *
     * @param player     the player that the AI will identify as
     * @param board      the Tic Tac Toe board to play on
     * @param currentPly the current depth
     * @return the score of the board
     */
    private static int getMax(TTTBoard.State player, TTTBoard board, int currentPly) {
        double bestScore = Double.NEGATIVE_INFINITY;
        int indexOfBestMove = -1;

        for (Integer theMove : board.getAvailableMoves()) {

            TTTBoard modifiedBoard = board.getDeepCopy();
            modifiedBoard.move(theMove);

            int score = miniMax(player, modifiedBoard, currentPly);

            if (score >= bestScore) {
                bestScore = score;
                indexOfBestMove = theMove;
            }

        }

        board.move(indexOfBestMove);
        return (int) bestScore;
    }

    /**
     * Play the move with the lowest score.
     *
     * @param player     the player that the AI will identify as
     * @param board      the Tic Tac Toe board to play on
     * @param currentPly the current depth
     * @return the score of the board
     */
    private static int getMin(TTTBoard.State player, TTTBoard board, int currentPly) {
        double bestScore = Double.POSITIVE_INFINITY;
        int indexOfBestMove = -1;

        for (Integer theMove : board.getAvailableMoves()) {

            TTTBoard modifiedBoard = board.getDeepCopy();
            modifiedBoard.move(theMove);

            int score = miniMax(player, modifiedBoard, currentPly);

            if (score <= bestScore) {
                bestScore = score;
                indexOfBestMove = theMove;
            }

        }

        board.move(indexOfBestMove);
        return (int) bestScore;
    }

    /**
     * Get the score of the board.
     *
     * @param player the play that the AI will identify as
     * @param board  the Tic Tac Toe board to play on
     * @return the score of the board
     */
    private static int score(TTTBoard.State player, TTTBoard board) {
        if (player == TTTBoard.State.Blank) {
            throw new IllegalArgumentException("Player must be X or O.");
        }

        TTTBoard.State opponent = (player == TTTBoard.State.X) ? TTTBoard.State.O : TTTBoard.State.X;

        if (board.isGameOver() && board.getWinner() == player) {
            return 10;
        } else if (board.isGameOver() && board.getWinner() == opponent) {
            return -10;
        } else {
            return 0;
        }
    }


}
