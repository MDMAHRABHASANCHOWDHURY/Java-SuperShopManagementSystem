package gui;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Simple methods for loading, resizing and saving pictures. */
public class ProductImages {

    public static BufferedImage read(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        // First check a selected file, then check the project's image folder.
        File file = new File(imagePath);
        if (!file.exists()) {
            file = new File("src", imagePath);
        }

        try {
            return ImageIO.read(file);
        } catch (IOException ex) {
            return null;
        }
    }

    public static ImageIcon icon(String imagePath, int width, int height) {
        BufferedImage picture = read(imagePath);
        if (picture == null) {
            return new ImageIcon();
        }
        Image smallPicture = picture.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(smallPicture);
    }

    public static String importImage(File selectedFile) throws IOException {
        return savePicture(selectedFile, "products");
    }

    public static String importProfileImage(File selectedFile) throws IOException {
        return savePicture(selectedFile, "profiles");
    }

    private static String savePicture(File selectedFile, String folderName) throws IOException {
        BufferedImage picture = ImageIO.read(selectedFile);
        if (picture == null) {
            throw new IOException("Please select a PNG or JPG picture.");
        }

        File folder = new File("src/images/" + folderName);
        folder.mkdirs();

        // The time gives each uploaded picture a different file name.
        long number = System.currentTimeMillis();
        File outputFile = new File(folder, "custom-" + number + ".png");
        while (outputFile.exists()) {
            number++;
            outputFile = new File(folder, "custom-" + number + ".png");
        }

        if (!ImageIO.write(picture, "png", outputFile)) {
            throw new IOException("Could not save the picture.");
        }
        return "images/" + folderName + "/" + outputFile.getName();
    }

    public static void configureTable(JTable table) {
        table.setRowHeight(72);
        table.getColumnModel().getColumn(0).setMinWidth(72);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
