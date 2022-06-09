import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


class Debug {
    private static final String TEXT_RESET = "\u001B[0m";
    private static final String TEXT_RED = "\u001B[31m";
    private static final String TEXT_GREEN = "\u001B[32m";
    private static final String TEXT_YELLOW = "\u001B[33m";

    public enum messageType {
        INFO,
        WARNING,
        ERROR,
        Normal
    }
    public static void Println(String message, messageType type) {
        switch (type) {
            case INFO:
                System.out.println(TEXT_GREEN + "[INFO] " + TEXT_RESET + message);
                break;
            case WARNING:
                System.out.println(TEXT_YELLOW + "[WARNING] " + TEXT_RESET + message);
                break;
            case ERROR:
                System.out.println(TEXT_RED + "[ERROR] " + TEXT_RESET + message );
                break;
            case Normal:
                System.out.println(message);
                break;
        }
    } 
    public static void Print(String message, messageType type) {
        switch (type) {
            case INFO:
                System.out.print(TEXT_GREEN + "[INFO] " + TEXT_RESET + message);
                break;
            case WARNING:
                System.out.print(TEXT_YELLOW + "[WARNING] " + TEXT_RESET + message);
                break;
            case ERROR:
                System.out.print(TEXT_RED + "[ERROR] " + TEXT_RESET + message );
                break;
            case Normal:
                System.out.print(message);
                break;
        }
    } 
}

class FileManager {
    private String fileUrl = "";
    private File currentDirectory;
    private String[] helpList = new String[] {
        "help - show this list",
        "cd - change directory",
        "cd .. - go to parent directory",
        "ls - list files",
        "mkdir - create directory",
        "sc - show content of a file",
        "del - remove file",
        "exit - exit from program",
    };

    public void Start() {
        setFolder();
        readCommand();
    }

    private boolean exit = false;
    private void readCommand() {
        PrintCurrentDir();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try {
            input = reader.readLine();
            input = input.trim();
            String command = input.split(" ")[0];
            command = command.toLowerCase();
            switch (command) {
                case "cd":
                    if (input.split(" ").length == 1) {
                        Debug.Println("Please enter a directory!", Debug.messageType.ERROR);
                        break;
                    }
                    ChangeDirectory(input);
                    break;
                case "ls":
                    if (input.split(" ").length > 1) {
                        Debug.Println("Invalid format for this type of command", Debug.messageType.ERROR);
                        break;
                    }
                    ListFiles();
                break;
                case "mkdir":
                    if (input.split(" ").length == 1) {
                        Debug.Println("Please enter a directory!", Debug.messageType.ERROR);
                        break;
                    }
                    MakeDirectory(input);
                    break;
                case "sc":
                    if (input.split(" ").length == 1) {
                        Debug.Println("Please enter a file!", Debug.messageType.ERROR);
                        break;
                    }
                    ShowContent(input);
                    break;
                case "del":
                    if (input.split(" ").length == 1) {
                        Debug.Println("Please enter a directory!", Debug.messageType.ERROR);
                        break;
                    }
                    RemoveItem(input);
                    break;
                case "exit":
                    if (input.split(" ").length > 1) {
                        Debug.Println("Invalid format for this type of command", Debug.messageType.ERROR);
                        break;
                    }
                    exit = true;
                    break;
                case "help":
                    for (String info : helpList) {
                        Debug.Println(info, Debug.messageType.Normal);
                    }
                    break;
                default:
                    if (input.length() == 0) break;
                    Debug.Println("Invalid command. Type \"help\" to see the list of commands", Debug.messageType.ERROR);
                    break;
            }
        } catch (IOException e) {
            Debug.Println("Error reading command", Debug.messageType.ERROR);
        }   
        if (!exit) readCommand();
    }

    public void setStartDirectory(String url) {
        this.fileUrl = url;
    }

    private void setFolder() {
        try {
            currentDirectory = new File(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void PrintCurrentDir () {
        Debug.Print(currentDirectory.getAbsolutePath()+">>> ", Debug.messageType.Normal);
    }

    private void MakeDirectory(String input) {
        String dirName = input.split(" ")[1];
        File newDir = new File(currentDirectory.getAbsolutePath() + "/" + dirName);
        if (newDir.exists()) {
            Debug.Println("Directory already exists", Debug.messageType.ERROR);
            return;
        }
        if (newDir.mkdir()) {
            Debug.Println("Directory created", Debug.messageType.INFO);
        } else {
            Debug.Println("Error creating directory", Debug.messageType.ERROR);
        }
    }

    private void ShowContent(String input) {
        String fileName = input.split(" ")[1];
        File file = new File(currentDirectory.getAbsolutePath() + "/" + fileName);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                Debug.Println(nextLine, Debug.messageType.Normal);
            }
            reader.close();
        } catch (Exception e) {
            Debug.Println("Error reading file", Debug.messageType.ERROR);
            e.printStackTrace();
        }
    }

    private void RemoveItem(String input) {
        String itemName = input.split(" ")[1];
        File item = new File(currentDirectory.getAbsolutePath() + "/" + itemName);
        
        if (!item.exists()) {
            Debug.Println("Item does not exist", Debug.messageType.ERROR);
            return;
        }
        if (item.delete()) {
            Debug.Println("Item deleted", Debug.messageType.INFO);
        } else {
            Debug.Println("Error deleting item", Debug.messageType.ERROR);
        }
    }

    private void ChangeDirectory(String input) {
        String dir = input.split(" ")[1];
        dir = dir.replaceAll("\\\\", "/");

        if (dir.equals("..")) {
            if (currentDirectory.getParentFile() != null) {
                currentDirectory = currentDirectory.getParentFile();
            }
        } else {
            File newDir = new File(currentDirectory.getAbsolutePath() + "/" + dir);
            if (dir.substring(0, fileUrl.length()).equals(fileUrl)) {
                newDir = new File(dir);
            }
            if (!newDir.exists() || newDir.isHidden()) {
                Debug.Println("Directory does not exist or is not accessible", Debug.messageType.ERROR);
                return;
            }
            currentDirectory = newDir;
        }
    }

    private void ListFiles() {
        File[] files = currentDirectory.listFiles();
        if (files == null) {
            Debug.Println("An unexpected error occured while trying to list files", Debug.messageType.ERROR);
            return;
        }
        if (files.length == 0) {
            Debug.Println("Directory is empty", Debug.messageType.INFO);
            return;
        }
        Debug.Println( "\n" + "Directory: " + currentDirectory.getAbsolutePath() + "\n", Debug.messageType.Normal);
        
        for (File file : files) {
            if (file.isHidden()) continue;
            Debug.Println(file.getName(), Debug.messageType.INFO);
        }
    }
}


public class Main {
    public static void main(String[] args) {
         FileManager manager = new FileManager();
         manager.setStartDirectory("C:/");
         manager.Start();
        
    }
}
