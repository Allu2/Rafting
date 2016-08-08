/**
 * Code from http://stackoverflow.com/questions/1656797/how-to-read-a-file-into-string-in-java/5733074#5733074
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Iterator;

class FileReader {
    public static void main(String[] args)
            throws FileNotFoundException {
        File file = new File(args[0]);

        System.out.println(getFileContents(file));

        processFileLines(file, new LineProcessor() {
            @Override
            public void process(int lineNumber, String lineContents) {
                System.out.println(lineNumber + ": " + lineContents);
            }
        });
    }

    static String getFileContents(File file)
            throws FileNotFoundException {
        try (Scanner s = new Scanner(file).useDelimiter("\\Z")) {
            return s.next();
        }
    }

    static void processFileLines(File file, LineProcessor lineProcessor)
            throws FileNotFoundException {
        try (Scanner s = new Scanner(file).useDelimiter("\\n")) {
            for (int lineNumber = 1; s.hasNext(); ++lineNumber) {
                lineProcessor.process(lineNumber, s.next());
            }
        }
    }

    static interface LineProcessor {
        void process(int lineNumber, String lineContents);
    }
}