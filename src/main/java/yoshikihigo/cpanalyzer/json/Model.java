
package yoshikihigo.cpanalyzer.json;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model {

  @SerializedName("LatentBugs")
  @Expose
  public List<LatentBug> latentBugs = new ArrayList<>();
}
