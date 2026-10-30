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

    // Directions dans l'ordre : haut, bas, gauche, droite
    public final static int[] DX = {0, 0, -1, 1};
    public final static int[] DY = {-1, 1, 0, 0};
    public final static char[] TOUCHES = {'z', 's', 'q', 'd'};

    // Limite de récursion du DFS (évite un StackOverflowError)
    public final static int PROFONDEUR_MAX = 3000;

    /**
     * VARIABLES JEU
     */
    public int nbMouvements;
    private char[][] board;
    private int[] playerPos;
    private int rows;
    private int cols;
    // Position des objectifs, fixe pour un niveau (permet de redessiner un '.' quand le joueur repart)
    private boolean[][] objectifs;

    /**
     * VARIABLES SOLVEUR
     */
    public int etatsExplores;
    private String[] visites;   // table de hachage des états déjà vus
    private int nbVisites;
    private char[] pileCoups;   // pile des coups du chemin DFS courant
    private int longueurDFS;

    /**
     * NIVEAUX (une ligne de texte = une ligne de la grille)
     */
    public final static String[][] NIVEAUX = {
            {
                    "########",
                    "#      #",
                    "#  .   #",
                    "#  $   #",
                    "#  @   #",
                    "#      #",
                    "########"
            },
            {
                    "####",
                    "# .#",
                    "#  ###",
                    "#*@  #",
                    "#  $ #",
                    "#  ###",
                    "####"
            },
            {
                    "#########",
                    "#       #",
                    "# .$$.  #",
                    "#   @ $ #",
                    "#  .    #",
                    "#########"
            }
    };
    public int niveauActuel;

    public Main() {
        this.chargerNiveau(0);
    }

    // Copie le niveau demandé dans la grille de jeu (les lignes trop courtes sont complétées par du vide)
    public void chargerNiveau(int numero) {
        String[] niveau = NIVEAUX[numero];
        this.niveauActuel = numero;
        this.nbMouvements = 0;
        this.rows = niveau.length;
        this.cols = 0;
        for (String ligne : niveau) {
            if (ligne.length() > this.cols) {
                this.cols = ligne.length();
            }
        }

        this.board = new char[this.rows][this.cols];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.board[i][j] = j < niveau[i].length() ? niveau[i].charAt(j) : EMPTY;
                if (this.board[i][j] == PLAYER) {
                    this.playerPos = new int[]{j, i};
                }
            }
        }

        this.initObjectifs();
    }

    private void initObjectifs() {
        this.objectifs = new boolean[this.rows][this.cols];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.objectifs[i][j] = this.board[i][j] == GOAL || this.board[i][j] == VALIDATE_CRATE;
            }
        }
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
        return getCase != WALL;
    }

    public boolean estCaisse(char c) {
        return c == CRATE || c == VALIDATE_CRATE;
    }

    public void deplacerJoueur(int x, int y) {
        int destX = this.playerPos[0] + x;
        int destY = this.playerPos[1] + y;

        char getCase = this.getCase(destX, destY);

        if (!deplacementValide(getCase)) {
            return;
        }
        if (estCaisse(getCase) && !this.deplacerCaisse(destX + x, destY + y)) {
            return;
        }

        // On remet un objectif sous le joueur s'il y en avait un
        int px = this.playerPos[0];
        int py = this.playerPos[1];
        this.setCase(px, py, this.objectifs[py][px] ? GOAL : EMPTY);
        this.playerPos[0] = destX;
        this.playerPos[1] = destY;
        this.setCase(this.playerPos[0], this.playerPos[1], PLAYER);
        this.nbMouvements++;
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

    public boolean partieTerminee() {
        if (!estVictoire(this.encoder(this.board))) {
            return false;
        }

        System.out.println("Victoire !");
        System.out.println("Nombre de mouvements : " + this.nbMouvements + "\n");

        return true;
    }


    // Résolution automatique

    public String encoder(char[][] tab) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                sb.append(tab[i][j]);
            }
        }
        return sb.toString();
    }

    public char[][] decoder(String tabApplati) {
        char[][] snapshotTab = new char[this.rows][this.cols];
        int indice = 0;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                snapshotTab[i][j] = tabApplati.charAt(indice);
                indice ++;
            }
        }
        return snapshotTab;
    }

    public int[] trouverJoueur() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (this.board[i][j] == PLAYER) {
                    return new int[]{j, i};
                }
            }
        }
        return null;
    }

    // Victoire = plus aucune caisse hors objectif
    public boolean estVictoire(String etat) {
        return etat.indexOf(CRATE) == -1;
    }

    // Caisse hors objectif coincée dans un coin : elle ne pourra plus jamais bouger
    public boolean estBloque(String etat) {
        for (int i = 0; i < etat.length(); i++) {
            if (etat.charAt(i) == CRATE) {
                boolean murHaut = etat.charAt(i - this.cols) == WALL;
                boolean murBas = etat.charAt(i + this.cols) == WALL;
                boolean murGauche = etat.charAt(i - 1) == WALL;
                boolean murDroite = etat.charAt(i + 1) == WALL;
                if ((murHaut || murBas) && (murGauche || murDroite)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Applique un mouvement à un état encodé, renvoie le nouvel état ou null si le mouvement est impossible
    public String etatSuivant(String etat, int dx, int dy) {
        this.board = this.decoder(etat);
        this.playerPos = this.trouverJoueur();
        int avant = this.nbMouvements;
        this.deplacerJoueur(dx, dy);
        if (this.nbMouvements == avant) {
            return null;
        }
        return this.encoder(this.board);
    }

    // Table de hachage (adressage ouvert) des états déjà visités
    private void viderVisites() {
        this.visites = new String[1024];
        this.nbVisites = 0;
    }

    // Renvoie true si l'état est nouveau (et l'ajoute), false s'il a déjà été visité
    private boolean ajouterVisite(String etat) {
        if (this.nbVisites * 2 >= this.visites.length) {
            String[] ancien = this.visites;
            this.visites = new String[ancien.length * 2];
            this.nbVisites = 0;
            for (String e : ancien) {
                if (e != null) {
                    this.ajouterVisite(e);
                }
            }
        }
        int indice = (etat.hashCode() & 0x7fffffff) % this.visites.length;
        while (this.visites[indice] != null) {
            if (this.visites[indice].equals(etat)) {
                return false;
            }
            indice = (indice + 1) % this.visites.length;
        }
        this.visites[indice] = etat;
        this.nbVisites++;
        return true;
    }

    /**
     * BFS : explore les états par distance croissante grâce à une file.
     * La file est faite de tableaux parallèles : etats[], parent[] et coup[] (pour reconstruire le chemin).
     * Renvoie la suite de touches de la solution (la plus courte), ou null si aucune solution.
     */
    public String resoudreBFS() {
        char[][] sauvegardeBoard = this.decoder(this.encoder(this.board));
        int[] sauvegardePos = {this.playerPos[0], this.playerPos[1]};
        int sauvegardeMouvements = this.nbMouvements;

        String depart = this.encoder(this.board);
        String[] etats = new String[1024];
        int[] parent = new int[1024];
        char[] coup = new char[1024];
        int tete = 0; // prochain état à défiler
        int fin = 0;  // prochaine case libre pour enfiler

        this.viderVisites();
        this.ajouterVisite(depart);
        etats[fin] = depart;
        parent[fin] = -1;
        fin++;
        this.etatsExplores = 0;

        String solution = null;
        while (tete < fin) {
            int courant = tete;
            tete++;
            this.etatsExplores++;

            if (this.estVictoire(etats[courant])) {
                // Remonte les parents pour retrouver les coups joués
                StringBuilder sb = new StringBuilder();
                for (int i = courant; parent[i] != -1; i = parent[i]) {
                    sb.append(coup[i]);
                }
                solution = sb.reverse().toString();
                break;
            }

            for (int d = 0; d < 4; d++) {
                String suivant = this.etatSuivant(etats[courant], DX[d], DY[d]);
                if (suivant != null && !this.estBloque(suivant) && this.ajouterVisite(suivant)) {
                    if (fin == etats.length) {
                        etats = java.util.Arrays.copyOf(etats, fin * 2);
                        parent = java.util.Arrays.copyOf(parent, fin * 2);
                        coup = java.util.Arrays.copyOf(coup, fin * 2);
                    }
                    etats[fin] = suivant;
                    parent[fin] = courant;
                    coup[fin] = TOUCHES[d];
                    fin++;
                }
            }
        }

        this.board = sauvegardeBoard;
        this.playerPos = sauvegardePos;
        this.nbMouvements = sauvegardeMouvements;
        return solution;
    }

    /**
     * DFS récursif : va le plus loin possible dans une direction avant de revenir en arrière.
     * Renvoie la suite de touches de la solution trouvée (pas forcément la plus courte), ou null.
     */
    public String resoudreDFS() {
        char[][] sauvegardeBoard = this.decoder(this.encoder(this.board));
        int[] sauvegardePos = {this.playerPos[0], this.playerPos[1]};
        int sauvegardeMouvements = this.nbMouvements;

        String depart = this.encoder(this.board);
        this.viderVisites();
        this.ajouterVisite(depart);
        this.pileCoups = new char[PROFONDEUR_MAX];
        this.etatsExplores = 0;

        String solution = null;
        if (this.dfs(depart, 0)) {
            solution = new String(this.pileCoups, 0, this.longueurDFS);
        }

        this.board = sauvegardeBoard;
        this.playerPos = sauvegardePos;
        this.nbMouvements = sauvegardeMouvements;
        return solution;
    }

    private boolean dfs(String etat, int profondeur) {
        this.etatsExplores++;

        // Cas de base : toutes les caisses sont sur un objectif
        if (this.estVictoire(etat)) {
            this.longueurDFS = profondeur;
            return true;
        }
        // Condition d'arrêt : trop profond, on abandonne cette branche
        if (profondeur >= PROFONDEUR_MAX) {
            return false;
        }

        // Cas récursif : on essaie chaque direction menant à un état jamais vu
        for (int d = 0; d < 4; d++) {
            String suivant = this.etatSuivant(etat, DX[d], DY[d]);
            if (suivant != null && !this.estBloque(suivant) && this.ajouterVisite(suivant)) {
                this.pileCoups[profondeur] = TOUCHES[d]; // empile le coup
                if (this.dfs(suivant, profondeur + 1)) {
                    return true;
                }
                // sinon le coup sera écrasé au prochain essai (dépile)
            }
        }
        return false;
    }

    // Rejoue la solution pas à pas sur la grille actuelle
    public void afficherSolution(String solution) {
        System.out.println("Solution (" + solution.length() + " mouvements) : " + solution);
        System.out.print("Voir le déroulé pas à pas ? (o/n) : ");
        if (!scanner.nextLine().equals("o")) {
            return;
        }
        this.afficherGrille();
        for (int i = 0; i < solution.length(); i++) {
            for (int d = 0; d < 4; d++) {
                if (TOUCHES[d] == solution.charAt(i)) {
                    this.deplacerJoueur(DX[d], DY[d]);
                }
            }
            System.out.println("Coup " + (i + 1) + " : " + solution.charAt(i));
            this.afficherGrille();
        }
        this.partieTerminee();
    }

    public void resoudreAutomatiquement() {
        menuAffichage("RESOLUTION", new String[]{"1. DFS (profondeur)", "2. BFS (largeur)"});
        System.out.print("Votre choix : ");
        String choix = scanner.nextLine();

        String solution;
        if (choix.equals("1")) {
            solution = this.resoudreDFS();
        } else if (choix.equals("2")) {
            solution = this.resoudreBFS();
        } else {
            System.out.println("Commande invalide.");
            return;
        }

        System.out.println("États explorés : " + this.etatsExplores);
        if (solution == null) {
            System.out.println("Aucune solution trouvée.");
        } else {
            this.afficherSolution(solution);
        }
    }

    public void comparerAlgorithmes() {
        long debut = System.nanoTime();
        String solDFS = this.resoudreDFS();
        long tempsDFS = System.nanoTime() - debut;
        int exploresDFS = this.etatsExplores;

        debut = System.nanoTime();
        String solBFS = this.resoudreBFS();
        long tempsBFS = System.nanoTime() - debut;
        int exploresBFS = this.etatsExplores;

        System.out.println("========== COMPARAISON ==========");
        afficherResultat("DFS", solDFS, exploresDFS, tempsDFS);
        afficherResultat("BFS", solBFS, exploresBFS, tempsBFS);
    }

    private static void afficherResultat(String nom, String solution, int explores, long tempsNs) {
        System.out.println(nom);
        System.out.println("Solution trouvée : " + (solution != null ? "oui" : "non"));
        if (solution != null) {
            System.out.println("Mouvements : " + solution.length());
        }
        System.out.println("États explorés : " + explores);
        System.out.println("Temps : " + (tempsNs / 1000) + " µs\n");
    }

    public static Scanner scanner = new Scanner(System.in);

    public void jouer() {
        //scanner, affiche les commandes, la grille

        System.out.println("Voici les commandes : z, q, s, d, x");
        this.afficherGrille();

        if(partieTerminee()) {
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
        menuAffichage("SOKOBAN JAVA", new String[]{"1. Nouvelle partie (niveau " + (this.niveauActuel + 1) + ")", "2. Choisir un niveau",
                "3. Résoudre automatiquement", "4. Comparer DFS / BFS", "6. Quitter"});
        System.out.print("Votre choix : ");

        // Un seul Scanner sur System.in (deux Scanner se volent les lignes tamponnées)
        String command = scanner.nextLine().trim();
        switch (command) {
            case "1":
                this.chargerNiveau(this.niveauActuel);
                jouer();
                break;
            case "2":
                if (this.choisirNiveau()) {
                    jouer();
                }
                break;
            case "3":
                this.chargerNiveau(this.niveauActuel);
                this.resoudreAutomatiquement();
                break;
            case "4":
                this.chargerNiveau(this.niveauActuel);
                this.comparerAlgorithmes();
                break;
            case "6":
                return;
            default:
                System.out.println("Commande invalide.");
                break;
        }
        startGame();
    }

    // Demande un numéro de niveau et le charge ; renvoie false si la saisie est invalide
    public boolean choisirNiveau() {
        String[] options = new String[NIVEAUX.length];
        for (int i = 0; i < NIVEAUX.length; i++) {
            options[i] = (i + 1) + ". Niveau " + (i + 1);
        }
        menuAffichage("NIVEAUX", options);
        System.out.print("Votre choix : ");

        String saisie = scanner.nextLine().trim();
        int numero;
        try {
            numero = Integer.parseInt(saisie);
        } catch (NumberFormatException e) {
            System.out.println("Commande invalide.");
            return false;
        }
        if (numero < 1 || numero > NIVEAUX.length) {
            System.out.println("Ce niveau n'existe pas.");
            return false;
        }
        this.chargerNiveau(numero - 1);
        return true;
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
        Main main = new Main();
        main.startGame();
    }
}
