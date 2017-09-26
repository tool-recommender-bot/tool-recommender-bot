# PullRecommender
Jenkins plugin to automatically recommend Google's Error Prone static analysis tool in pull requests for open source Java projects.

Set Up:
* Install necessary jar files
* Create a file .github.creds that contains your github username and password on separate lines

Run from source code:
```
$ sudo javac -cp jcabi-github-0.23-jar-with-dependencies.jar:error_prone_ant-2.1.0.jar:gumtree.jar:$GUMTREE_HOME com/chbrown13/pull_rec/ErrorProneItem.java com/chbrown13/pull_rec/PullRecommender.java com/chbrown13/pull_rec/Utils.java
$ java -cp .:jcabi-github-0.23-jar-with-dependencies.jar:error_prone_ant-2.1.0.jar:gumtree.jar com.chbrown13.pull_rec.PullRecommender <owner> <repo>
```
