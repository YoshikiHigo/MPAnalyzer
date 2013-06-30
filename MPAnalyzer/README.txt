MPAnanlyzer is a tool for deriving patterns of source code modifications from software repositories.
Derived modifications are stored into a SQL database and users can extract some of them by using SQL queries because patterns are characterized by some metrics.
A simple GUI is included in MPAnalyzer and it can be used for checking extracted patterns.


- Target Software

Currently, MPAnalyzer handles only JAVA software being managed with SVN repositories.


- Configuration

MPAnalyzer has many options, so configurations are passed to MPAnalyzer not by command line parameters but by a configuration file.
The file name for the configuration is "config.txt".
A template for the configuration is included in this package, which is "config_template.txt".
Please rename the file to "config.txt" and specify your setting for each parameter.

- Output

MPAnalyzer uses SQLite for storing extracted patterns.
If you use MPAnalyzer in Eclipse environment, I would recommend you to use DBVeiwer (http://www.ne.jp/asahi/zigen/home/plugin/dbviewer/about_en.html) for browsing data in the database.
You would be able to use a simple GUI included in MPAnalyzer for checking extracted modification patterns.
Execute MainWindow located in directory "gui" with no parameters for using the GUI.



