/**
 * <h1>License :</h1> <br>
 * The following code is deliver as is. I take care that code compile and work, but I am not responsible about any damage it may
 * cause.<br>
 * You can use, modify, the code as your need for any usage. But you can't do any action that avoid me or other person use,
 * modify this code. The code is free for usage and modification, you can't change that fact.<br>
 * <br>
 * 
 * @author JHelp
 */
package jhelp.video;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.ImageIcon;

import jhelp.util.io.FileComparator;
import jhelp.util.io.UtilIO;

/**
 * Make a video from a directory<br>
 * This directory must only contains each image play in the video.<br>
 * The order of images based on there name, so its recommended to name images like that "image_0000.jpg", "image_0001.jpg",
 * "image_0002.jpg", ... Remember "10" is before "9" because "10" start with "1", so use "10" and "09".<br>
 * All images must have same width and same height. This is not control for performance reason, but if it is false you can have
 * exception or strange result on reading the video <br>
 * Last modification : 27 sept. 2008<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class VideoMaker
{
   /**
    * Make a video from a directory<br>
    * This directory must only contains each image play in the video.<br>
    * The order of images based on there name, so its recommended to name images like that "image_0000.jpg", "image_0001.jpg",
    * "image_0002.jpg", ... Remember "10" is before "9" because "10" start with "1", so use "10" and "09".<br>
    * All images must have same width and same height. This is not control for performance reason, but if it is false you can
    * have exception or strange result on reading the video
    * 
    * @param directory
    *           Directory where images are
    * @param outputStream
    *           Where write the video
    * @throws IOException
    *            On reading/writing problem
    */
   public static void makeVideo(final File directory, final OutputStream outputStream) throws IOException
   {
      ZipOutputStream zipOutputStream;
      File[] filesImage;
      ImageIcon imageIcon;
      int length;
      int index;
      FileInputStream fileInputStream;
      byte[] temp;
      ByteArrayOutputStream byteArrayOutputStream;
      int read;

      length = 0;
      // Get image list and sort it
      filesImage = directory.listFiles();
      if(filesImage != null)
      {
         length = filesImage.length;
         Arrays.sort(filesImage, FileComparator.FILE_COMPARATOR);
      }

      // Prepare the output
      zipOutputStream = new ZipOutputStream(outputStream);
      zipOutputStream.setLevel(9);
      zipOutputStream.putNextEntry(new ZipEntry("video"));

      // Write the number of images
      UtilIO.writeInteger(length, zipOutputStream);

      temp = new byte[4096];
      if(length > 0)
      {
         // Write video size
         imageIcon = new ImageIcon(filesImage[0].toURI().toURL());
         UtilIO.writeInteger(imageIcon.getIconWidth(), zipOutputStream);
         UtilIO.writeInteger(imageIcon.getIconHeight(), zipOutputStream);
         imageIcon = null;

         // For each image
         for(index = 0; index < length; index++)
         {
            // Get all bytes
            fileInputStream = new FileInputStream(filesImage[index]);
            byteArrayOutputStream = new ByteArrayOutputStream();
            read = fileInputStream.read(temp);
            while(read >= 0)
            {
               byteArrayOutputStream.write(temp, 0, read);

               read = fileInputStream.read(temp);
            }

            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            fileInputStream.close();
            fileInputStream = null;

            temp = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream = null;

            // Write the bytes (number, then image data them self
            UtilIO.writeInteger(temp.length, zipOutputStream);
            zipOutputStream.write(temp);
            zipOutputStream.flush();
         }
      }
      // Close the video
      zipOutputStream.finish();
      zipOutputStream.flush();
      zipOutputStream.closeEntry();
      zipOutputStream.close();

      zipOutputStream = null;
      filesImage = null;
      imageIcon = null;
      fileInputStream = null;
      temp = null;
      byteArrayOutputStream = null;
   }
}