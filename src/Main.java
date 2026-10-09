import java.util.Scanner;

public class Main {

    private final char[][] board;
    private int[] playerPos;

    /**
     * CONSTANTES
     */
    public final static char WALL = '#';
    public final static char EMPTY = ' ';
    public final static char PLAYER = '@';
    public final static char CRATE = '$';
    public final static char GOAL = '.';
    public final static char VALIDATE_CRATE = '*';

    public Main(int rows, int cols) {
        this.board = new char[rows][cols];
        this.initBoard();
    }

    private void initBoard() {
        int rows = this.board.length;
        int cols = this.board[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0 || j == 0 || i == rows - 1 || j == cols - 1) {
                    this.board[i][j] = WALL;
                } else {
                    this.board[i][j] = EMPTY;
                }
            }
        }

        this.playerPos = new int[]{3, 4};
        this.deplacerJoueur(this.playerPos[0], this.playerPos[1]);
        this.board[3][3] = CRATE;
        this.board[2][3] = GOAL;

    }

    public void afficherGrille() {
        for (char[] chars : this.board) {
            for (char c : chars) {
                System.out.print(c + "");
            }
            System.out.println();
        }
    }

    public boolean deplacementValide(int x, int y) {
        return this.board[y][x] != WALL;
    }

    public void deplacerJoueur(int x, int y) {
        if (!deplacementValide(x, y)) {
            return;
        }
        this.board[this.playerPos[1]][this.playerPos[0]] = EMPTY;
        this.playerPos[0] = x;
        this.playerPos[1] = y;
        this.board[this.playerPos[1]][this.playerPos[0]] = PLAYER;
    }

    public void jouer() {
        //scanner, affiche les commandes, la grille
        System.out.println("Voici les commandes : z,q,s,d, x");
        this.afficherGrille();
        System.out.print("Votre commande : ");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        if (command.length() > 1) {
            System.out.println("Invalide.");
            jouer();
            return;
        }
        switch (command) {
            case "z":
                this.deplacerJoueur(this.playerPos[0], this.playerPos[1]-1);
                break;
            case "q":
                this.deplacerJoueur(this.playerPos[0]-1, this.playerPos[1]);
                break;
            case "s":
                this.deplacerJoueur(this.playerPos[0], this.playerPos[1]+1);
                break;
            case "d":
                this.deplacerJoueur(this.playerPos[0]+1, this.playerPos[1]);
                break;
            case "x":
                System.out.println("Quitter");
                return;
            default:
                System.out.println("Invalide.");
                break;
        }
        jouer();
    }


    public void startGame() {
        menuAffichage("SOKOBAN JAVA", new String[]{"1. Nouvelle partie", "2. Choisir un niveau"});

        Scanner scanner = new Scanner(System.in);
        int command = scanner.nextInt();
        switch (command) {
            case 1:
                jouer();
                break;
            case 2:
                break;
        }
    }

    public static void menuAffichage(String title, String[] options){
        int len_max = title.length();
        for(String opt : options) {
            if(opt.length() > len_max) {
                len_max = opt.length() + 1;
            }
        }
        System.out.println("╭" + "─".repeat(len_max) + "╮");
        int lenTopbar = (len_max - title.length()) / 2;
        System.out.println("│"+ " ".repeat(lenTopbar) + title + " ".repeat(lenTopbar+1) + "│");
        System.out.println("├" + "─".repeat(len_max) + "┤");
        for(String opt : options) {
            System.out.println("│" + opt + " ".repeat(len_max-opt.length()) + "│");
        }
        System.out.println("╰" + "─".repeat(len_max) + "╯");
    }

    public static void main(String[] args) {
        Main main = new Main(7, 8);
        main.startGame();
        Scanner scan = new Scanner(System.in);//.useDelimiter("\n");
    }
}
