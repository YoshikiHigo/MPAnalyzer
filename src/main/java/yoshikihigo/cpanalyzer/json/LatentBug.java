
package yoshikihigo.cpanalyzer.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LatentBug {

  @SerializedName("file")
  @Expose
  public String file;
  @SerializedName("fromLine")
  @Expose
  public Integer fromLine;
  @SerializedName("toLine")
  @Expose
  public Integer toLine;
  @SerializedName("patternID")
  @Expose
  public Integer patternID;

}
