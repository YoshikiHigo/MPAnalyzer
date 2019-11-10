
package yoshikihigo.cpanalyzer.json;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model {

  @SerializedName("TargetType")
  @Expose
  public String targetType = null;

  @SerializedName("GitRepo")
  @Expose
  public String gitRepo = null;

  @SerializedName("GitCommit")
  @Expose
  public String gitCommit = null;

  @SerializedName("SvnRepo")
  @Expose
  public String svnRepo = null;

  @SerializedName("SvnRevision")
  @Expose
  public Integer svnRevision = null;

  @SerializedName("LocalDir")
  @Expose
  public String localDir = null;

  @SerializedName("LatentBugs")
  @Expose
  public List<LatentBug> latentBugs = new ArrayList<>();
}
