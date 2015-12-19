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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

import jhelp.util.io.UtilIO;

/**
 * For read a video create by VideoMaker<br>
 * <br>
 * Last modification : 27 sept. 2008<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class VideoReader
{
   /** Actual image read index */
   private int            actualImage;
   /** Base file (If we constructs from file) */
   private File           file;
   /** Video height */
   private int            height;
   /** Number of image in video */
   private int            imageCount;
   /** Video name */
   private final String   name;
   /** Base URL (If we construct from URL) */
   private URL            url;
   /** Video width */
   private int            width;
   /** Stream that read video */
   private ZipInputStream zipInputStream;

   /**
    * Constructs VideoReader
    * 
    * @param file
    *           Video file
    * @throws IOException
    *            On reading problem
    */
   public VideoReader(final File file)
         throws IOException
   {
      if(file == null)
      {
         throw new NullPointerException("The file musn't be null !");
      }
      this.name = file.getAbsolutePath();
      this.file = file;
      this.initialize(new FileInputStream(file));
   }

   /**
    * Constructs VideoReader
    * 
    * @param name
    *           Video name
    * @param inputStream
    *           Stream for read video
    * @throws IOException
    *            On reading problem
    */
   public VideoReader(final String name, final InputStream inputStream)
         throws IOException
   {
      if(inputStream == null)
      {
         throw new NullPointerException("The inputStream musn't be null !");
      }
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      this.name = name;
      this.initialize(inputStream);
   }

   /**
    * Constructs VideoReader
    * 
    * @param url
    *           Video URL
    * @throws IOException
    *            On reading problem
    */
   public VideoReader(final URL url)
         throws IOException
   {
      if(url == null)
      {
         throw new NullPointerException("The url musn't be null !");
      }
      this.name = url.toString();
      this.url = url;
      this.initialize(url.openStream());
   }

   /**
    * Initialize the video.<br>
    * Read header, that is to say : number of image and video size
    * 
    * @param inputStream
    *           Stream to read
    * @throws IOException
    *            On reading problem
    */
   private void initialize(final InputStream inputStream) throws IOException
   {
      this.zipInputStream = new ZipInputStream(inputStream);
      this.zipInputStream.getNextEntry();

      this.imageCount = UtilIO.readInteger(this.zipInputStream);
      if(this.imageCount > 0)
      {
         this.width = UtilIO.readInteger(this.zipInputStream);
         this.height = UtilIO.readInteger(this.zipInputStream);
      }

      this.actualImage = 0;
   }

   /**
    * Indicates if the video have the capacity to be reset (Restart).<br>
    * The video build from file or URL can be restart, video from stream can't
    * 
    * @return {@code true} if the video have the capacity to be reset (Restart)
    */
   public boolean canResetVideo()
   {
      return (this.url != null) || (this.file != null);
   }

   /**
    * Close the video<br>
    * The video can be reopen if build from file or URL on using {@link #resetVideo()} method
    * 
    * @throws IOException
    *            On closing problem
    */
   public void closeVideo() throws IOException
   {
      if(this.isClosed() == true)
      {
         return;
      }

      this.zipInputStream.closeEntry();
      this.zipInputStream.close();
      this.zipInputStream = null;

      this.actualImage = this.imageCount;
   }

   /**
    * Return actualImage
    * 
    * @return actualImage
    */
   public int getActualImage()
   {
      return this.actualImage;
   }

   /**
    * Return height
    * 
    * @return height
    */
   public int getHeight()
   {
      return this.height;
   }

   /**
    * Return imageCount
    * 
    * @return imageCount
    */
   public int getImageCount()
   {
      return this.imageCount;
   }

   /**
    * Return name
    * 
    * @return name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Return width
    * 
    * @return width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * Indicates if there are an other image to read
    * 
    * @return {@code true} if there are an other image to read
    */
   public boolean hasNextImage()
   {
      return this.actualImage < this.imageCount;
   }

   /**
    * Indicates if the video is closed
    * 
    * @return {@code true} if the video is closed
    */
   public boolean isClosed()
   {
      return this.zipInputStream == null;
   }

   /**
    * The next image.<br>
    * If we are at the end of the video :
    * <ul>
    * <li>If the video build from file or URL, then the video restart from the first image.</li>
    * <li>If the video build from stream, then close the video</li>
    * </ul>
    * 
    * @return Next image
    * @throws IOException
    *            On reading problem
    */
   public Image nextImage() throws IOException
   {
      int size;
      byte[] data;
      int read;

      if(this.hasNextImage() == false)
      {
         throw new IllegalStateException("No more image");
      }

      size = UtilIO.readInteger(this.zipInputStream);
      data = new byte[size];
      read = this.zipInputStream.read(data, 0, size);
      while(read < size)
      {
         read += this.zipInputStream.read(data, read, size - read);
      }

      this.actualImage++;
      if(this.actualImage >= this.imageCount)
      {
         if(this.canResetVideo() == true)
         {
            this.resetVideo();
         }
         else
         {
            this.closeVideo();
         }
      }

      final Image image = Toolkit.getDefaultToolkit().createImage(data);
      data = null;

      return image;
   }

   /**
    * Restart the video from the first image<br>
    * Only video build from file or URL can do this.
    * 
    * @throws IOException
    *            On reset problem
    */
   public void resetVideo() throws IOException
   {
      if(this.canResetVideo() == false)
      {
         throw new IllegalStateException("This video can't be reset. Only video constructs with file or URL can be reset");
      }

      if(this.isClosed() == false)
      {
         this.closeVideo();
      }

      if(this.url != null)
      {
         this.initialize(this.url.openStream());
         return;
      }

      this.initialize(new FileInputStream(this.file));
   }
}