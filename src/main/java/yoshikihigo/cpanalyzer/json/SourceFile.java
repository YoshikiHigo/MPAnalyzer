package yoshikihigo.cpanalyzer.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SourceFile {

  public SourceFile(final String path) {
    this.path = path;
  }

  @SerializedName("path")
  @Expose
  public String path;
}
