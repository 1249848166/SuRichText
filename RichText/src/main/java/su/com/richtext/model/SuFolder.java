package su.com.richtext.model;

public class SuFolder{
    String folderName;
    String folderPath;
    String firstFilePath;
    int num;

    public SuFolder(String folderName, String folderPath, String firstFilePath, int num) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.firstFilePath = firstFilePath;
        this.num = num;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public void setFirstFilePath(String firstFilePath) {
        this.firstFilePath = firstFilePath;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getFirstFilePath() {
        return firstFilePath;
    }
}
