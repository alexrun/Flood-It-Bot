package flooditbot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/*
 * Trabajar con la clase Robot. 
 * @author aNNiMON y Alexrun
 */
public class RobotUtils {

    private static final int CLICK_DELAY = 300;
    private Robot robot;

    /*
     * diseñador
     * @ Throws inicialización AWTException Robot error
     */
    public RobotUtils() throws AWTException {
        robot = new Robot();
    }

    /*
     * Haga clic en el punto deseado
     * @ Param clic en un punto en el que deberá hacer clic en
     */
    public void clickPoint(Point click) {
        robot.mouseMove(click.x, click.y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }
    
    /*
     *  Reproducción automática de una determinada secuencia de pulsaciones de teclas
      * @ Param botones de las coordenadas del lugar donde desea hacer clic en
      * @ Param id resultado secuencia para especificar la tecla deseada
     */
    public void autoClick(Point[] buttons, byte[] result) {
        for (int i = 0; i < result.length; i++) {
            clickPoint(buttons[result[i]]);
        }
    }
    /*
     * Los mensajes de la escritura automática
     * @param text "impreso" de Texto
     */
    public void writeMessage(String text) {
        for (char symbol : text.toCharArray()) {
            boolean needShiftPress = Character.isUpperCase(symbol) && Character.isLetter(symbol);
            if(needShiftPress) {
                robot.keyPress(KeyEvent.VK_SHIFT);
            }
            int event = KeyEvent.getExtendedKeyCodeForChar(symbol);
            try {
                robot.keyPress(event);
            } catch (Exception e) {}
            if(needShiftPress) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
    }
    
    /*
     * Conseguir la imagen a tamaño [ancho x alto] en la posición de la pantalla [x, y]
     * Si la anchura o la altura es -1, y luego volver a la pantalla.
     */
    public BufferedImage getImage(int x, int y, int width, int height) {
        Rectangle area;
        if ((width == -1) || (height == -1)) {
            area = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        } else area = new Rectangle(x, y, width, height);
        return robot.createScreenCapture(area);
    }

}
