name := "messageboard"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	filters,
  "org.webjars" %% "webjars-play" % "2.2.0", 
  "org.webjars" % "bootstrap" % "3.0.2",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)


resolvers ++= Seq(
 	"Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
 )
  
play.Project.playScalaSettings
