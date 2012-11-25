package main;

import java.util.ArrayList;

/**
 * Clase lógica bot.
 * @author aNNiMON
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
    
    /**
     * Obtener el color de las células de la paleta
     * @ Devolver una matriz de colores RGB
     */
    public int[] getColors() {
        return colors;
    }
    
    /**
     * Obtener la secuencia del relleno de color
     * @ Devolver una matriz de ID para el color de relleno
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
                maxArea = fillRate[i];
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
                    // Добавляем цвет в палитру
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
    
    /**
     * Vierta el campo especificado el uso del color
     * @ Param tabla el campo de juego para llenar
     * @ Param color del color de relleno
     */
    private void fillTable(byte[][] table, byte color) {
        if (table[0][0] == color) return;
        fill(table, 0, 0, table[0][0], color);
    }
    
    /*
     * Заливка поля по координатам
     */
    private void fill(byte[][] table, int x, int y, byte prevColor, byte color) {
        // Проверка на выход за границы игрового поля
        if ( (x < 0) || (y < 0) || (x >= table.length) || (y >= table.length) ) return;
        if (table[x][y] == prevColor) {
            table[x][y] = color;
            // Заливаем смежные области
            fill(table, x-1, y, prevColor, color);
            fill(table, x+1, y, prevColor, color);
            fill(table, x, y-1, prevColor, color);
            fill(table, x, y+1, prevColor, color);
        }
    }
    
    /**
     * Получить количество залитых ячеек
     * @param table игровое поле
     */
    private int getFillCount(byte[][] table) {
        return getCount(table, 0, 0, table[0][0]);
    }
    
    /*
     * Подсчет залитых ячеек по координатам
     */
    private int getCount(byte[][] table, int x, int y, byte color) {
        // Проверка на выход за границы игрового поля
        if ( (x < 0) || (y < 0) || (x >= table.length) || (y >= table.length) ) return 0;
        int count = 0;
        if (table[x][y] == color) {
            table[x][y] = -1;
            count = 1;
            // Считаем смежные ячейки
            count += getCount(table, x-1, y, color);
            count += getCount(table, x+1, y, color);
            count += getCount(table, x, y-1, color);
            count += getCount(table, x, y+1, color);
        }
        return count;
    }
    
    /*
     * Проверка, залита ли вся область одним цветом
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
     * Копирование массива игрового поля
     */
    private byte[][] copyTable(byte[][] table) {
        int size = table.length;
        byte[][] out = new byte[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(table[i], 0, out[i], 0, size);
        }
        return out;
    }
}
