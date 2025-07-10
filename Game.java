import java.util.*;

abstract class Piece {
    String color;
    char symbol;

    Piece(String color, char symbol) {
        this.color = color;
        this.symbol = symbol;
    }

    abstract List<Position> availableMoves(int x, int y, Map<Position, Piece> board);

    boolean isValid(Position start, Position end, Map<Position, Piece> board) {
        return availableMoves(start.x, start.y, board).contains(end);
    }
}

class Position {
    int x, y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

class Rook extends Piece {
    Rook(String color) {
        super(color, color.equals("white") ? 'R' : 'r');
    }

    List<Position> availableMoves(int x, int y, Map<Position, Piece> board) {
        List<Position> res = new ArrayList<>();
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            while (Game.inBounds(nx, ny)) {
                Position p = new Position(nx, ny);
                if (!board.containsKey(p)) res.add(p);
                else {
                    if (!board.get(p).color.equals(color)) res.add(p);
                    break;
                }
                nx += d[0]; ny += d[1];
            }
        }
        return res;
    }
}

class Bishop extends Piece {
    Bishop(String color) {
        super(color, color.equals("white") ? 'B' : 'b');
    }

    List<Position> availableMoves(int x, int y, Map<Position, Piece> board) {
        List<Position> res = new ArrayList<>();
        int[][] dirs = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : dirs) {
            int nx = x + d[0], ny = y + d[1];
            while (Game.inBounds(nx, ny)) {
                Position p = new Position(nx, ny);
                if (!board.containsKey(p)) res.add(p);
                else {
                    if (!board.get(p).color.equals(color)) res.add(p);
                    break;
                }
                nx += d[0]; ny += d[1];
            }
        }
        return res;
    }
}

class Queen extends Piece {
    Queen(String color) {
        super(color, color.equals("white") ? 'Q' : 'q');
    }

    List<Position> availableMoves(int x, int y, Map<Position, Piece> board) {
        List<Position> res = new Rook(color).availableMoves(x, y, board);
        res.addAll(new Bishop(color).availableMoves(x, y, board));
        return res;
    }
}

class Knight extends Piece {
    Knight(String color) {
        super(color, color.equals("white") ? 'N' : 'n');
    }

    List<Position> availableMoves(int x, int y, Map<Position, Piece> board) {
        List<Position> res = new ArrayList<>();
        int[][] moves = {{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}};
        for (int[] m : moves) {
            int nx = x + m[0], ny = y + m[1];
            if (Game.noConflict(board, color, nx, ny)) res.add(new Position(nx, ny));
        }
        return res;
    }
}

class King extends Piece {
    King(String color) {
        super(color, color.equals("white") ? 'K' : 'k');
    }

    List<Position> availableMoves(int x, int y, Map<Position, Piece> board) {
        List<Position> res = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    int nx = x + dx, ny = y + dy;
                    if (Game.noConflict(board, color, nx, ny)) res.add(new Position(nx, ny));
                }
            }
        }
        return res;
    }
}

class Pawn extends Piece {
    int dir;
    Pawn(String color, int dir) {
        super(color, color.equals("white") ? 'P' : 'p');
        this.dir = dir;
    }

    List<Position> availableMoves(int x, int y, Map<Position, Piece> board) {
        List<Position> res = new ArrayList<>();
        Position fwd = new Position(x, y + dir);
        if (!board.containsKey(fwd)) res.add(fwd);
        for (int dx = -1; dx <= 1; dx += 2) {
            Position diag = new Position(x + dx, y + dir);
            if (board.containsKey(diag) && !board.get(diag).color.equals(color)) res.add(diag);
        }
        return res;
    }
}

public class Game {
    Map<Position, Piece> board = new HashMap<>();
    String turn = "black";

    public static boolean inBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public static boolean noConflict(Map<Position, Piece> board, String color, int x, int y) {
        if (!inBounds(x, y)) return false;
        Piece p = board.get(new Position(x, y));
        return p == null || !p.color.equals(color);
    }

    public Game() {
        setup();
        loop();
    }

    void setup() {
        for (int i = 0; i < 8; i++) {
            board.put(new Position(i, 1), new Pawn("white", 1));
            board.put(new Position(i, 6), new Pawn("black", -1));
        }
        Piece[] whitePieces = {new Rook("white"), new Knight("white"), new Bishop("white"), new Queen("white"),
                new King("white"), new Bishop("white"), new Knight("white"), new Rook("white")};
        Piece[] blackPieces = {new Rook("black"), new Knight("black"), new Bishop("black"), new Queen("black"),
                new King("black"), new Bishop("black"), new Knight("black"), new Rook("black")};

        for (int i = 0; i < 8; i++) {
            board.put(new Position(i, 0), whitePieces[i]);
            board.put(new Position(i, 7), blackPieces[i]);
        }
    }

    void loop() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            printBoard();
            if (isCheckmate(turn)) {
                System.out.println("Checkmate! " + (turn.equals("white") ? "black" : "white") + " wins.");
                break;
            }
            System.out.print(turn + " to move. Enter move (e.g. e2 e4): ");
            String a = sc.next(), b = sc.next();
            Position p1 = parse(a), p2 = parse(b);
            if (!inBounds(p1.x, p1.y) || !inBounds(p2.x, p2.y)) {
                System.out.println("Invalid coordinates."); continue;
            }
            Piece piece = board.get(p1);
            if (piece == null || !piece.color.equals(turn)) {
                System.out.println("Invalid move."); continue;
            }
            if (!piece.isValid(p1, p2, board)) {
                System.out.println("Move not allowed for piece."); continue;
            }
            board.remove(p2);
            board.put(p2, piece);
            board.remove(p1);
            turn = turn.equals("white") ? "black" : "white";
        }
        sc.close();
    }

    boolean isCheckmate(String color) {
        for (Map.Entry<Position, Piece> entry : board.entrySet()) {
            if (!entry.getValue().color.equals(color)) continue;
            Position pos = entry.getKey();
            Piece piece = entry.getValue();
            for (Position to : piece.availableMoves(pos.x, pos.y, board)) {
                Piece captured = board.get(to);
                board.put(to, piece);
                board.remove(pos);
                boolean found = board.values().stream().anyMatch(p -> p.symbol == (color.equals("white") ? 'K' : 'k'));
                board.put(pos, piece);
                if (captured != null) board.put(to, captured);
                else board.remove(to);
                if (found) return false;
            }
        }
        return true;
    }

    Position parse(String s) {
        return new Position(s.charAt(0) - 'a', s.charAt(1) - '1');
    }

    void printBoard() {
        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println("  +---+---+---+---+---+---+---+---+");
        for (int y = 7; y >= 0; y--) {
            System.out.print((y+1) + " |");
            for (int x = 0; x < 8; x++) {
                Piece p = board.get(new Position(x, y));
                System.out.print(" " + (p != null ? p.symbol : ' ') + " |");
            }
            System.out.println(" " + (y+1));
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }
        System.out.println("    a   b   c   d   e   f   g   h\n");
    }

    public static void main(String[] args) {
        new Game();
    }
}
