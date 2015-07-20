package yoshikihigo.cpanalyzer;

public enum LANGUAGE {

	C("C") {
		@Override
		public String[] getExtensions() {
			return new String[] { ".c", ".h" };
		}
	},

	CPP("CPP") {
		@Override
		public String[] getExtensions() {
			return new String[] { ".cc", "cpp", "cxx", ".hh", "hpp", "hxx" };
		}
	},

	JAVA("JAVA") {
		@Override
		public String[] getExtensions() {
			return new String[] { ".java" };
		}
	},

	PYTHON("PYTHON") {
		@Override
		public String[] getExtensions() {
			return new String[] { ".py" };
		}
	};

	final public String value;

	private LANGUAGE(final String value) {
		this.value = value;
	}

	abstract public String[] getExtensions();

	final public boolean isTarget(final String name) {
		for (final String extension : this.getExtensions()) {
			if (name.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}
}
