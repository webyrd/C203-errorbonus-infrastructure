/*
  TODO:

  Improve error handling.
  Should gracefully handle the case in which the program has already been run.
  Generally do a better job of checking eroneous conditions.

  Do we want to be strict about directory structure and names?  Notice
  that '__MACOSX' directories submitted by macOS users can contain
  subdirectories and file names that match the expected submitted
  files.

  Refactor, and make code more general, for general autograding.

  It would be interesting to share this code with C203 students, as an
  example of a real-world Java program.  Especially if we can have
  them help us improve the autograder, and the general style of the
  code.
*/


/*
  Java API documents are available at:
  https://docs.oracle.com/en/java/javase/13/docs/api/index.html

  For example:  
  https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/io/File.html
*/
import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;

public class ErrorBonus {

  /*
    Given a target directory 'dir', do the following for each '<foo>.zip'
    file within the top-level of 'dir':
    1) create a new directory named '<foo>'
    2) unzip '<foo>.zip', putting the unzipped contents in '<foo>'
    3) look for a directory named 'errorbonus' inside of '<foo>'
    4) if found, run 'wc' on all '.java' files in the directory, and direct the output to '<foo>_errorbonus.txt' in 'dir'
  */
  public static void main(String[] args) {

    if (args.length != 1) {
      System.out.println("usage: java ErrorBonus <directory name>");
      System.exit(1);
    }

    System.out.println("=============================");
    
    System.out.println(args[0]);
    File hw3_dir = new File(args[0]);
    String hw3_dir_path = hw3_dir.getAbsolutePath();

    String unzipProgram = "unzip";

    java.io.FilenameFilter zipFilter = (File dir, String name)->name.endsWith(".zip");
    File[] zipFiles = hw3_dir.listFiles​(zipFilter);

    for (File zipFile: zipFiles) {

      System.out.println("----------------------------");
      
      System.out.println(zipFile);
      String dirName = zipFile.getName().replaceFirst​(".zip$", "");
      System.out.println(dirName);
      String dirPath = hw3_dir_path + File.separator + dirName;
      System.out.println(dirPath);
      String[] zipCommandArray = {unzipProgram, zipFile.getAbsolutePath(), "-d", dirPath};
      System.out.println();
      
      for (String cmd : zipCommandArray) {
        System.out.println(cmd);
      }
      System.out.println();
      
      // Process code adapted from:
      // https://stackoverflow.com/questions/9346077/java-execute-command-line-program
      try {
        Process tr = Runtime.getRuntime().exec(zipCommandArray);
        try {
          tr.waitFor();
        } catch (InterruptedException ex) {
          System.out.println("*** unzip interrupted! ");
          System.out.println(ex);
          throw new RuntimeException(ex);
        }

        File unzippedDir = new File(dirPath);
        System.out.println("unzippedDir for dirPath " + dirPath + " is " + unzippedDir);

        System.out.println();
        System.out.println("absolutePath: " + unzippedDir.getAbsolutePath());
        System.out.println("exists: " + unzippedDir.exists());
        System.out.println("isDirectory: " + unzippedDir.isDirectory());
        System.out.println("isFile: " + unzippedDir.isFile());
        System.out.println();
        
        File[] files = unzippedDir.listFiles();
        System.out.println("files " + files);
        
        for (File f : files) {
          System.out.println(f);
        }
        System.out.println();
        System.out.println();

        File errorbonusDir = findErrorbonusDirectory(unzippedDir);
        if (errorbonusDir != null) {
          System.out.println("found errorbonus dir: " + errorbonusDir.getName());

          java.io.FilenameFilter javaFilter = (File dir, String name)->name.endsWith(".java");
          String[] javaFileNames = errorbonusDir.list​(javaFilter);

          // Adapted from https://beginnersbook.com/2013/12/how-to-convert-array-to-arraylist-in-java/
          ArrayList<String> cmdArrayList = new ArrayList<String>(Arrays.asList(javaFileNames));
          Collator myCollator = Collator.getInstance();
          cmdArrayList.sort(myCollator);
          cmdArrayList.add(0, "wc");
          String[] wcCmdArray = cmdArrayList.toArray​(new String[0]);
          
          System.out.println();
      
          for (String cmd : wcCmdArray) {
            System.out.println(cmd);
          }
          System.out.println();
          
          try {
            Process p = Runtime.getRuntime().exec(wcCmdArray, null, errorbonusDir);
            OutputStream output = new FileOutputStream(hw3_dir_path + File.separator + dirName + "error_bonus.txt");
            // Adapted from:
            // https://stackoverflow.com/questions/43157/easy-way-to-write-contents-of-a-java-inputstream-to-an-outputstream
            p.getInputStream().transferTo(output);
            p.getInputStream().close();
            output.close();
            
            try {
              p.waitFor();
            } catch (InterruptedException ex) {
              System.out.println("*** wc interrupted! ");
              System.out.println(ex);
              throw new RuntimeException(ex);
            }

            
          } catch (java.io.IOException ex) {
            System.out.println("*** wc failed! ");
            System.out.println(ex);
            throw new RuntimeException(ex);
          }

        }
        
      } catch (java.io.IOException ex) {
        System.out.println("*** unzip failed! ");
        System.out.println(ex);
        throw new RuntimeException(ex);
      }
    }    
  }

  private static File findErrorbonusDirectory(File dir) {
    if (dir == null) {
      return null;
    }
    if (dir.getName().equals("__MACOSX")) {
      return null;
    }
    if (dir.getName().equals("errorbonus")) {
      return dir;
    }
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
        File res = findErrorbonusDirectory(f);
        if (res != null) {
          return res;
        }
      }
    }
    return null;
  }
  
}
