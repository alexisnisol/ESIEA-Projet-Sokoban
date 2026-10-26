import java.util.Scanner;

public class Main {

    /**
     * CONSTANTES
     */
    public final static char WALL = '#';
    public final static char EMPTY = ' ';
    public final static char PLAYER = '@';
    public final static char CRATE = '$';
    public final static char GOAL = '.';
    public final static char VALIDATE_CRATE = '*';


    /**
     * VARIABLES JEU
     */
    public static int nbMouvements = 0;
    private final char[][] board;
    private int[] playerPos;

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
        this.setCase(this.playerPos[0], this.playerPos[1], PLAYER);

        this.setCase(3, 3, CRATE);
        this.setCase(3, 2, GOAL);
    }

    public void setCase(int x, int y, char c) {
        this.board[y][x] = c;
    }

    public char getCase(int x, int y) {
        return this.board[y][x];
    }

    public void afficherGrille() {
        for (char[] chars : this.board) {
            for (char c : chars) {
                System.out.print(c + "");
            }
            System.out.println();
        }
    }

    public boolean deplacementValide(char getCase) {
        return getCase != WALL && getCase != VALIDATE_CRATE;
    }

    public void deplacerJoueur(int x, int y) {
        int destX = this.playerPos[0] + x;
        int destY = this.playerPos[1] + y;

        char getCase = this.getCase(destX, destY);

        if (!deplacementValide(getCase)) {
            return;
        }
        if (getCase == CRATE && !this.deplacerCaisse(destX + x, destY + y)) {
            return;
        }

        this.setCase(this.playerPos[0], this.playerPos[1], EMPTY);
        this.playerPos[0] = destX;
        this.playerPos[1] = destY;
        this.setCase(this.playerPos[0], this.playerPos[1], PLAYER);
        Main.nbMouvements++;
    }

    public boolean deplacerCaisse(int destX, int destY) {
        char getCase = this.getCase(destX, destY);
        if (this.peutPousser(getCase)) {
            if (getCase == GOAL) {
                this.setCase(destX, destY, VALIDATE_CRATE);
            } else {
                //TODO: Remplacer la case précédente de la caisse en EMPTY (Pas obligatoire car le joueur prend la place de la caisse, selon la seule mécanique du jeu : il pousse la caisse
                this.setCase(destX, destY, CRATE);
            }
            return true;
        }
        return false;
    }

    public boolean peutPousser(char getCase) {
        //TODO vérifier hors map?
        return getCase == EMPTY || getCase == GOAL;
    }

    public boolean verifierVictoire() {
        for (char[] chars : this.board) {
            for (char c : chars) {
                if (c == GOAL) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Scanner scanner = new Scanner(System.in);

    public void jouer() {
        //scanner, affiche les commandes, la grille

        System.out.println("Voici les commandes : z, q, s, d, x");
        this.afficherGrille();

        if(verifierVictoire()) {
            System.out.println("Victoire !");
            System.out.println("Nombre de mouvements : " + Main.nbMouvements + "\n");
            System.out.print("Entrer pour continuer...");
            scanner.nextLine();
            return;
        }

        System.out.print("Votre commande : ");

        String command = scanner.nextLine();
        if (command.length() > 1) {
            System.out.println("Invalide.");
            jouer();
            return;
        }
        switch (command) {
            case "z":
                this.deplacerJoueur(0, -1);
                break;
            case "s":
                this.deplacerJoueur(0, 1);
                break;
            case "q":
                this.deplacerJoueur(-1, 0);
                break;
            case "d":
                this.deplacerJoueur(1, 0);
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
        menuAffichage("SOKOBAN JAVA", new String[]{"1. Nouvelle partie", "2. Choisir un niveau", "6. Quitter"});

        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextInt()) {
            int command = scanner.nextInt();
            switch (command) {
                case 1:
                    Main.nbMouvements = 0;
                    jouer();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Commande invalide.");
                    break;
            }
        } else {
            System.out.println("Commande invalide.");
        }
        startGame();
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
    }
}
