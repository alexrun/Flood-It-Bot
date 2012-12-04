package flooditbot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/*
 * Clase de imágenes
 * @author aNNiMON
 */
public class ImageUtils {
    
    /* ¿Cuántos puntos aprendemos el fondo predominante? */
    private static final int MAX_COLOR_POINTS = 50;
    
    /* Sensibilidad para buscar el botón  */
    private static final int FIND_BUTTON_TOLERANCE = 20;
    
    /*  Imagen de la ventana  */
    private BufferedImage image;
    /* Tamaño de la imagen */
    private int w, h;
    /* La dimensión del campo */
    private int boardSize;
    /* El tamaño de las células */
    private int cellSize;
    /* Coordenada ángulo jugar campo */
    private Point board;
    /* Representación Negro y blanco de la imagen */
    private boolean[] monochrome;
    
    /*
     *  Diseñador para definir los ajustes
     * @ Param imagen
     * @ Param boardSize
     */
    public ImageUtils(BufferedImage image, int boardSize) {
        this.image = image;
        this.boardSize = boardSize;
        w = image.getWidth();
        h = image.getHeight();
    }
    
    /*
     * Diseñador para comprobar los ajustes
     * @ Param imagen
     * @ Param boardSize
     * @ Param cellsize
     * @ Param x
     * @ Param y
     */
    public ImageUtils(BufferedImage image, int boardSize, int cellSize, int x, int y) {
        this.image = image;
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.board = new Point(x, y);
        w = image.getWidth();
        h = image.getHeight();
    }
    
    /*
     * Obtener el tamaño de una célula
     * @ Return
     */
    public int getCellSize() {
        return cellSize;
    }
    
    /*
     * Obtener las coordenadas del terreno de juego
     * @ Return el punto con las coordenadas de la esquina superior izquierda del campo
     */
    public Point getBoardParameters() {
        int[] pixels = new int[w * h];
        image.getRGB(0, 0, w, h, pixels, 0, w);
        monochrome = threshold(pixels, 64);
        board = getBoardXY(boardSize);
        return board;
    }
    
    /*
     * Obtener un campo de imagen de juego
     * @ Devolver la imagen igualdad de condiciones
     */
    public BufferedImage getBoardImage() {
        int size = cellSize * boardSize;
        try {
            return image.getSubimage(board.x, board.y, size, size);
        } catch (Exception e) {
            return image;
        }
    }
    
    /*
     * Obtener las coordenadas de los botones de clic de forma automática
     * @ Param colores variedad de colores, lo que busca el botón
     * @ Devolver una matriz de puntos de coordenadas, o null - si usted no puede encontrar
     */
    public Point[] getButtons(int[] colors) {
        Point[] out = new Point[colors.length];
        // Tamaño del campo de juego, en píxeles
        int size = boardSize * cellSize;
        // Importe de la imagen, que buscará el botón
        Rectangle[] partsOfImage = new Rectangle[] {
            new Rectangle(0, board.y, board.x, size),   // слева от поля
            new Rectangle(0, 0, w, board.y),            // сверху от поля
            new Rectangle(board.x+size, board.y,
                          w-board.x-size, size),        // справа от поля
            new Rectangle(0, board.y+size,
                          w, h-board.y-size)            // снизу от поля
        };
        
        for (int i = 0; i < partsOfImage.length; i++) {
            Rectangle rect = partsOfImage[i];
            BufferedImage part = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            // Clipping región, en la que se busca
            boolean found = true;
            for (int j = 0; j < colors.length; j++) {
                if (colors[i] == -1) continue;
                Point pt = findButton(part, colors[j]);
                if (pt != null) {
                    // Tener en cuenta la parte compensado sólo de imagen
                    pt.translate(rect.x, rect.y);
                    out[j] = pt;
                } else {
                    found = false;
                    break;
                }
            }
            if (found) return out;
        }
        // No se puede encontrar todos los puntos
        return null;
    }
    
    /*
     * Convierte una variedad de colores en la vista gráfica
     * @ Param array ids identificador de secuencia
     * @ Param array paleta de paletas de colores
     * @ Return La secuencia de imágenes en color
     */
    public BufferedImage sequenceToImage(byte[] ids, int[] palette) {
        final int size = 20; // tamaño de cada celda
        // Romperá con 10 celdas por línea
        final int CELLS_IN_ROW = 10;
        int width = CELLS_IN_ROW * size;
        if (width == 0) width = size;
        int rows = ids.length / CELLS_IN_ROW;
        
        BufferedImage out = new BufferedImage(width, (rows*size)+size, BufferedImage.TYPE_INT_RGB);
        Graphics G = out.getGraphics();
        for (int i = 0; i < ids.length; i++) {
            G.setColor(new Color(palette[ids[i]]));
            G.fillRect(i % CELLS_IN_ROW * size,
                       i / CELLS_IN_ROW * size,
                       size, size);
        }
        G.dispose();
        return out;
    }
    
    /*
     * Преобразовать цветное изображение в монохромное.
     * Нужно также учесть, что если поле расположено на светлом
     * фоне, то необходимо инвертировать изображение, чтобы
     * получить сплошную белую область на месте поля.
     * @param pixels массив пикселей изображения
     * @param value разделяющее значение
     * @return массив boolean, true - белый, false - чёрный
     */
    private boolean[] threshold(int[] pixels, int value) {
        boolean inverse = isBackgroundLight(MAX_COLOR_POINTS);
        if (inverse) value = 255 - value;
        boolean[] bw = new boolean[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int brightNess = getBrightness(pixels[i]);
            bw[i] = (brightNess >= value) ^ inverse;
        }
        return bw;
    }
    
    /*
     * Получение состояния яркости фона.
     * @param numPoints сколько точек нужно для определения.
     * @return true - фон светлый, false - тёмный
     */
    private boolean isBackgroundLight(int numPoints) {
        // Получаем numPoints случайных точек
        Random rnd = new Random();
        int[] colors = new int[numPoints];
        for (int i = 0; i < numPoints; i++) {
            int x = rnd.nextInt(w);
            int y = rnd.nextInt(h);
            colors[i] = image.getRGB(x, y);
        }
        // Находим среднюю яркость всех numPoints точек
        long sum = 0;
        for (int i = 0; i < numPoints; i++) {
            int brightness = getBrightness(colors[i]);
            sum = sum + brightness;
        }
        sum = sum / numPoints;
        return (sum > 128);
    }
    
    /*
     * Определить координаты левой верхней ячейки игрового поля.
     * @param boardSize размерность поля (10x10, 14x14 и т.д.)
     * @return координата левого верхнего прямоугольника
     */
    private Point getBoardXY(int boardSize) {
        /*
         * Сначала подсчитаем количество белых точек по горизонтали и вертикали
         */
        int[] horizontal = new int[h];
        for (int i = 0; i < h; i++) {
            int count = 0;
            for (int j = 0; j < w; j++) {
                if (getBWPixel(j, i)) count++;
            }
            horizontal[i] = count;
        }

        int[] vertical = new int[w];
        for (int i = 0; i < w; i++) {
            int count = 0;
            for (int j = 0; j < h; j++) {
                if (getBWPixel(i, j)) count++;
            }
            vertical[i] = count;
        }
        
        /*
         * Затем "отфильтруем" лишнее: подсчитаем среднее значение
         * и на его основе уберём малозначимые строки и столбцы.
         */
        horizontal = filterByMean(horizontal);
        vertical = filterByMean(vertical);
        
        /*
         * Ищем наибольшую ненулевую последовательность.
         * Индексы границ последовательности и будут граничными точками поля.
         */
        int[] vParam = getParamsFromSequence(horizontal);
        int[] hParam = getParamsFromSequence(vertical);
        

        int outX = hParam[0];
        int outY = vParam[0];
        int outWidth = hParam[1];
        int outHeight = vParam[1];
        // Подсчет размера ячейки
        cellSize = Math.max((outWidth / boardSize), (outHeight / boardSize));
        return new Point(outX, outY); 
    }
    
    /*
     * Фильтр последовательности от малозначимых значений.
     * @param source последовательность вхождений цвета
     * @return отфильтрованный массив со значениями 0 и 1
     */
    private int[] filterByMean(int[] source) {
        long mean = 0;
        for (int i = 0; i < source.length; i++) {
            mean += source[i];
        }
        mean = mean / source.length;
        for (int i = 0; i < source.length; i++) {
            source[i] = (source[i] > mean) ? 1 : 0;
        }
        return source;
    }
    
    /*
     * Поиск самой длинной последовательности в массиве.
     * @param source входная последовательность из нулей и единиц
     * @return массив параметров - индекс начала последовательности и её длина
     */
    private int[] getParamsFromSequence(int[] source) {
        int maxStart = 0, start = 0;
        int maxLength = 0, length = 0;
        for (int i = 1; i < source.length; i++) {
            if (source[i] == 0) {
                start = 0;
                length = 0;
                continue;
            }
            if (source[i] == source[i-1]) {
                length++;
                if (maxLength < length) {
                    maxStart = start;
                    maxLength = length;
                }
            } else {
                // Если предыдущий элемент был нулевым - начинаем новую последовательность
                start = i;
            }
        }
        return new int[] {maxStart, maxLength};
    }
    
    /*
     * Поиск координаты кнопки с цветом template
     * @param img изображение, на котором будем искать
     * @param template шаблон цвета
     * @return координата X. Y, или null если не нашли
     */
    private Point findButton(BufferedImage img, int template) {
        int h2 = img.getHeight() / 2;
        // Искать будем с середины по вертикали, так быстрее найдём
        for (int y = 0; y < h2; y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getRGB(x, h2 - y);
                if (isEquals(color, template, FIND_BUTTON_TOLERANCE)) {
                    return new Point(x, h2 - y);
                }
                color = img.getRGB(x, h2 + y);
                if (isEquals(color, template, FIND_BUTTON_TOLERANCE)) {
                    return new Point(x, h2 + y);
                }
            }
        }
        // Не нашли
        return null;
    }
    
    /*
     * Проверка на соответствие цветов друг другу
     * @param color1 первый цвет
     * @param color2 второй цвет
     * @param tolerance чувствительность
     * @return true - соответствуют, false - нет
     */
    private boolean isEquals(int color1, int color2, int tolerance) {
        if (tolerance < 2) return color1 == color2;

        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >> 8) & 0xff;
        int b1 = color1 & 0xff;
        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >> 8) & 0xff;
        int b2 = color2 & 0xff;
        return (Math.abs(r1 - r2) <= tolerance) &&
               (Math.abs(g1 - g2) <= tolerance) &&
               (Math.abs(b1 - b2) <= tolerance);
    }
    
    /*
     * Получение яркости цвета
     * @param color исходный цвет
     * @return яркость (0..255)
     */
    private int getBrightness(int color) {
        int qr = (color >> 16) & 0xff;
        int qg = (color >> 8) & 0xff;
        int qb = color & 0xff;
        return (qr + qg + qb) / 3;
    }
    
    /*
     * Получение цвета из монохромного изображения.
     * return true - белый, false - чёрный
     */
    private boolean getBWPixel(int x, int y) {
        if ((x < 0) || (y < 0) || (x >= w) || (y >= h)) return false;
        return monochrome[y * w + x];
    }

}