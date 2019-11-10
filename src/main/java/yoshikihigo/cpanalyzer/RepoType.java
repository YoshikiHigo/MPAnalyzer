package yoshikihigo.cpanalyzer;


public enum RepoType {

  GITREPO {

    @Override
    public String toString() {
      return "GITREPO";
    }
  },

  SVNREPO {

    @Override
    public String toString() {
      return "SVNREPO";
    }
  },

  LOCALDIR {

    @Override
    public String toString() {
      return "LOCALDIR";
    }
  };
}
