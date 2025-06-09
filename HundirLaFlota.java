
import java.util.*;

class Coordenada {
    int fila;
    int columna;
    boolean impactada;

    Coordenada(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
        this.impactada = false;
    }
}

abstract class Barco {
    int tamaño;
    List<Coordenada> posiciones = new ArrayList<>();
    boolean vertical;

    Barco(int tamaño, boolean vertical) {
        this.tamaño = tamaño;
        this.vertical = vertical;
    }

    boolean estaHundido() {
        for (Coordenada c : posiciones) {
            if (!c.impactada) return false;
        }
        return true;
    }
}

class Lancha extends Barco {
    Lancha(Coordenada inicio) {
        super(1, false);
        posiciones.add(inicio);
    }
}

class Buque extends Barco {
    Buque(Coordenada inicio, boolean vertical) {
        super(3, vertical);
        for (int i = 0; i < 3; i++) {
            posiciones.add(vertical ? new Coordenada(inicio.fila + i, inicio.columna)
                                   : new Coordenada(inicio.fila, inicio.columna + i));
        }
    }
}

class Acorazado extends Barco {
    Acorazado(Coordenada inicio, boolean vertical) {
        super(4, vertical);
        for (int i = 0; i < 4; i++) {
            posiciones.add(vertical ? new Coordenada(inicio.fila + i, inicio.columna)
                                   : new Coordenada(inicio.fila, inicio.columna + i));
        }
    }
}

class Portaaviones extends Barco {
    Portaaviones(Coordenada inicio, boolean vertical) {
        super(5, vertical);
        for (int i = 0; i < 5; i++) {
            posiciones.add(vertical ? new Coordenada(inicio.fila + i, inicio.columna)
                                   : new Coordenada(inicio.fila, inicio.columna + i));
        }
    }
}

class Tablero {
    char[][] celdas;
    List<Barco> barcos;
    int filas, columnas;

    Tablero(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        celdas = new char[filas][columnas];
        barcos = new ArrayList<>();
        for (int i = 0; i < filas; i++) Arrays.fill(celdas[i], '-');
    }

    boolean insertarBarco(Barco b, char simbolo) {
        for (Coordenada c : b.posiciones) {
            if (c.fila >= filas || c.columna >= columnas || celdas[c.fila][c.columna] != '-') return false;
        }
        for (Coordenada c : b.posiciones) {
            celdas[c.fila][c.columna] = simbolo;
        }
        barcos.add(b);
        return true;
    }

    boolean disparar(int fila, int columna) {
        for (Barco b : barcos) {
            for (Coordenada c : b.posiciones) {
                if (c.fila == fila && c.columna == columna) {
                    c.impactada = true;
                    return true;
                }
            }
        }
        return false;
    }

    void mostrar(boolean oculto) {
        System.out.print("  ");
        for (int c = 0; c < columnas; c++) System.out.print(c + " ");
        System.out.println();
        for (int i = 0; i < filas; i++) {
            System.out.print((char) ('A' + i) + " ");
            for (int j = 0; j < columnas; j++) {
                char celda = celdas[i][j];
                if (!oculto && celda != '-' && celda != 'A' && celda != 'X') System.out.print(celda + " ");
                else if (celda == 'X' || celda == 'A') System.out.print(celda + " ");
                else System.out.print("- ");
            }
            System.out.println();
        }
    }

    void actualizarTableroVisible(int fila, int columna, boolean impacto) {
        celdas[fila][columna] = impacto ? 'X' : 'A';
    }

    boolean todosHundidos() {
        for (Barco b : barcos) if (!b.estaHundido()) return false;
        return true;
    }
}

class Jugador {
    Scanner scanner = new Scanner(System.in);

    Coordenada disparar() {
        while (true) {
            try {
                System.out.print("Introduce coordenada (ej: A5): ");
                String input = scanner.nextLine().toUpperCase();
                int fila = input.charAt(0) - 'A';
                int columna = Integer.parseInt(input.substring(1));
                if (fila >= 0 && fila < 10 && columna >= 0 && columna < 10) {
                    return new Coordenada(fila, columna);
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida. Intenta de nuevo.");
            }
        }
    }

    int elegirNivel() {
        System.out.println("Elige dificultad: 1. Fácil 2. Medio 3. Difícil 4. Personalizado");
        return scanner.nextInt();
    }

    int[] configuracionPersonalizada() {
        System.out.println("Introduce filas, columnas, #Lanchas, #Buques, #Acorazados, #Portaaviones, intentos:");
        int[] cfg = new int[7];
        for (int i = 0; i < 7; i++) cfg[i] = scanner.nextInt();
        return cfg;
    }
}

public class Juego {
    Tablero tableroOculto;
    Tablero tableroVisible;
    Jugador jugador;
    int intentosRestantes;
    int filas = 10, columnas = 10;

    public void iniciar() {
        jugador = new Jugador();
        int nivel = jugador.elegirNivel();
        int lanchas = 0, buques = 0, acorazados = 0, portaaviones = 0;

        switch (nivel) {
            case 1: intentosRestantes = 50; lanchas = 5; buques = 3; acorazados = 1; portaaviones = 1; break;
            case 2: intentosRestantes = 30; lanchas = 2; buques = 1; acorazados = 1; portaaviones = 1; break;
            case 3: intentosRestantes = 10; lanchas = 1; buques = 1; break;
            case 4:
                int[] cfg = jugador.configuracionPersonalizada();
                filas = cfg[0]; columnas = cfg[1]; lanchas = cfg[2]; buques = cfg[3];
                acorazados = cfg[4]; portaaviones = cfg[5]; intentosRestantes = cfg[6];
                break;
        }

        tableroOculto = new Tablero(filas, columnas);
        tableroVisible = new Tablero(filas, columnas);
        colocarBarcos(lanchas, buques, acorazados, portaaviones);
        bucleDeJuego();
    }

    private void colocarBarcos(int lanchas, int buques, int acorazados, int portaaviones) {
        Random rand = new Random();
        while (lanchas-- > 0) {
            while (!tableroOculto.insertarBarco(new Lancha(new Coordenada(rand.nextInt(filas), rand.nextInt(columnas))), 'L')) {}
        }
        while (buques-- > 0) {
            while (true) {
                Coordenada c = new Coordenada(rand.nextInt(filas), rand.nextInt(columnas));
                boolean vertical = rand.nextBoolean();
                Buque b = new Buque(c, vertical);
                if (tableroOculto.insertarBarco(b, 'B')) break;
            }
        }
        while (acorazados-- > 0) {
            while (true) {
                Coordenada c = new Coordenada(rand.nextInt(filas), rand.nextInt(columnas));
                boolean vertical = rand.nextBoolean();
                Acorazado a = new Acorazado(c, vertical);
                if (tableroOculto.insertarBarco(a, 'Z')) break;
            }
        }
        while (portaaviones-- > 0) {
            while (true) {
                Coordenada c = new Coordenada(rand.nextInt(filas), rand.nextInt(columnas));
                boolean vertical = rand.nextBoolean();
                Portaaviones p = new Portaaviones(c, vertical);
                if (tableroOculto.insertarBarco(p, 'P')) break;
            }
        }
    }

    private void bucleDeJuego() {
        while (intentosRestantes > 0 && !tableroOculto.todosHundidos()) {
            tableroVisible.mostrar(true);
            Coordenada disparo = jugador.disparar();
            boolean impacto = tableroOculto.disparar(disparo.fila, disparo.columna);
            tableroVisible.actualizarTableroVisible(disparo.fila, disparo.columna, impacto);
            System.out.println(impacto ? "¡Tocado!" : "Agua.");
            intentosRestantes--;
            System.out.println("Intentos restantes: " + intentosRestantes);
        }
        finalizarJuego();
    }

    private void finalizarJuego() {
        if (tableroOculto.todosHundidos()) System.out.println("¡Has ganado!");
        else System.out.println("¡Has perdido!");
        System.out.println("Tablero del ordenador:");
        tableroOculto.mostrar(false);
    }

    public static void main(String[] args) {
        new Juego().iniciar();
    }
}
