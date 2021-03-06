package flooditbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/*
 * Clase lógica bot.
 * @author aNNiMON y Alexrun
 */
public class BotFloodIt {
    
    /* Número de colores en el juego */
    private static final int MAX_COLORS = 6;
    /* ¿Cuántos pasos recuento carrera hacia adelante? */
    private static final int FILL_STEPS = 4;
    
    /* El campo de juego */
    private byte[][] table;
    /* Color correspondiente al ID */
    private int[] colors;
    
    public BotFloodIt(int[][] table) {
        colors = new int[MAX_COLORS];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = -1;
        }
        this.table = colorsToIds(table);
    }
    
    /*
     * Obtener el color de las células de la paleta
     * @Devolver una matriz de colores RGB
     */
    public int[] getColors() {
        return colors;
    }
    
    /*
     * Obtener la secuencia del relleno de color
     * @Devolver una matriz de ID para el color de relleno
     */
    public byte[] getFillSequence() {
        byte[][] copyTable = copyTable(table);
        ArrayList<Byte> seq = new ArrayList<Byte>();
        while(!gameCompleted(copyTable)) {
            seq.add(getNextFillColor(copyTable));
        }
        byte[] out = new byte[seq.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = seq.get(i).byteValue();
            System.out.println("Secuencia: " + seq.toString());
        }        
        return out;
    }
    
    /*
     * Obtener el índice del color junto a llenar
     */
    private byte getNextFillColor(byte[][] table) {
        // Número de opciones llena
        int fillSize = (int) Math.pow(MAX_COLORS, FILL_STEPS);
        int[] fillRate = new int[fillSize];
        // Rellenar los valores del grado de llenado
        int[] fillPow = new int[FILL_STEPS];
        for (int i = 0; i < FILL_STEPS; i++) {
            fillPow[i] = (int) Math.pow(MAX_COLORS, i);
        }
        // Llenar FILL_STEPS MAX_COLORS opciones de tiempo
        for (int i = 0; i < fillSize; i++) {
            byte[][] iteration = copyTable(table);
            for (int j = 0; j < FILL_STEPS; j++) {
                byte fillColor =  (byte) (i / fillPow[j] % MAX_COLORS);
                fillTable(iteration, fillColor);
            }
            // Cuenta la cantidad de celdas llenas
            fillRate[i] = getFillCount(iteration);
        }
        // Ahora busca la máxima área inundada de iteraciones FILL_STEPS llenar
        int maxArea = fillRate[0];
        int maxColor = 0;
        for (int i = 1; i < fillSize; i++) {
            if (fillRate[i] > maxArea) {
                maxColor = i;
                maxArea = fillRate[i] ;
            }
        }
        // Obtiene el color con la mayor superficie de más de relleno
        byte colorID = (byte) (maxColor % MAX_COLORS);
        fillTable(table, colorID);
        return colorID;
    }
    
    /*
     * Convertir una matriz de colores en una matriz de ID
     */
    private byte[][] colorsToIds(int[][] tableColor) {
        int size = tableColor.length;
        byte[][] out = new byte[size][size];
        int colorsReaded = 1; // número de colores reconocidos
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int color = tableColor[i][j];
                for (byte k = 0; k < colorsReaded; k++) {
                    // Añadir color a la paleta
                    if (colors[k] == -1) {
                        colors[k] = color;
                        colorsReaded++;
                        if (colorsReaded > MAX_COLORS) colorsReaded = MAX_COLORS;
                    }
                    // Si el color está en la paleta, luego asignarle ID
                    if (color == colors[k]) {
                        out[i][j] = k;
                        break;
                    }
                }
            }
        }
        return out;
    }
    
    /*
     * Vierte al campo especificado el uso del color
     * @Param tabla el campo de juego para llenar
     * @Param color del color de relleno
     */
    private void fillTable(byte[][] table, byte color) {
        if (table[0][0] == color) return;
        fill(table, 0, 0, table[0][0], color);
    }
    
    /*
     * Complete con las coordenadas del campo
     */
    private void fill(byte[][] table, int x, int y, byte prevColor, byte color) {
        // Salida de los límites del campo de juego
        if ( (x < 0) || (y < 0) || (x >= table.length) || (y >= table.length) ) return;
        if (table[x][y] == prevColor) {
            table[x][y] = color;
            // Rellenar las áreas adyacentes
            fill(table, x-1, y, prevColor, color);
            fill(table, x+1, y, prevColor, color);
            fill(table, x, y-1, prevColor, color);
            fill(table, x, y+1, prevColor, color);
        }
    }
    
    /*
     * Obtener el número de células llenas
     * @Param tabla el campo de juego
     */
    private int getFillCount(byte[][] table) {
        return getCount(table, 0, 0, table[0][0]);
    }
    
    /*
     * Contar celdas llenas en las coordenadas
     */
    private int getCount(byte[][] table, int x, int y, byte color) {
        // Salida de los límites del campo de juego
        if ( (x < 0) || (y < 0) || (x >= table.length) || (y >= table.length) ) return 0;
        int count = 0;
        if (table[x][y] == color) {
            table[x][y] = -1;
            count = 1;
            // Creen que las células adyacentes
            count += getCount(table, x-1, y, color);
            count += getCount(table, x+1, y, color);
            count += getCount(table, x, y-1, color);
            count += getCount(table, x, y+1, color);
        }
             
        return count;
    }
    
    /*
     * Compruebe que toda la zona está llena de un color
     */
    private boolean gameCompleted(byte[][] table) {
        byte color = table[0][0];
        int size = table.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (table[i][j] != color) return false;
            }
        }
        return true;
    }
    
    /*
     * Copia matriz igualdad de condiciones
     */
    private byte[][] copyTable(byte[][] table) {
        int size = table.length;
        byte[][] out = new byte[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(table[i], 0, out[i], 0, size);
        }
        return out; 
     
    } 
      
    /*
     * Determina si se puede eliminar un color
     */
    private boolean canCompletColor(byte[][] table, byte eColor) {
        //contar el numero de cuadros del color determinado por eColor
        int countT = 0; // numero de cuadros de color = eColor
        int size = table.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (table[i][j] == eColor) {
                    countT++;                                    
                }
            }
        }
        //contar el numero de cuadros que cambiarian a el color eColor
        int countP = 0; // numero de cuadros de color = eColor proximo
        
        
        // return (countP != 0) && (countT == countP);
        if((countT == countP) && (countP != 0)){
            return true;
        }else{
            return false;
        }
    }
    
    
}

