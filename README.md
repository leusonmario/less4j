Less language is an extension of css and this project compiles it into regular css. It adds several dynamic features into css: variables, expressions, nested rules, and so on. 

The original compiler was written in JavaScript and is called [less.js](http://lesscss.org/). The less language is mostly defined in less.js documentation/issues and by what less.js actually do. Links to less.js:
* [less.js home page](http://lesscss.org/) 
* [less.js source code & issues](https://github.com/cloudhead/less.js) 

Less4j is a port and its behavior should be as close to the original implementation as reasonable. Unless explicitly stated otherwise, any difference between less.js and less4j outputs is considered a bug. As close as reasonable means that style sheets generated by less4j must be functionally the same as the outputs of less.js. However, they do not have to be exactly the same:
* Behavior of less.js and less4j may differ on invalid input files.
* Output files may differ by whitespaces or comments locations.
* Less4j may do more than less.js in some situations. The input rejected by less.js may be accepted and translated by less4j. 

All known differences are documented on [wiki page](https://github.com/SomMeri/less4j/wiki/Differences-Between-Less.js-and-Less4j). In the future, Less4j will produce warning any time it produces functionally different CSS. That feature was not implemented yet.

## Continuous Integration
Continuous integration is set up on [Travis-CI](http://travis-ci.org/SomMeri/less4j), its current status is: [![Build Status](https://secure.travis-ci.org/SomMeri/less4j.png)](http://travis-ci.org/SomMeri/less4j).

## Twitter
Our twitter account: [Less4j](https://twitter.com/Less4j)

## Documentation:
The documentation is kept on Github wiki:
* [wiki home page](https://github.com/SomMeri/less4j/wiki),
* [all written wiki pages](https://github.com/SomMeri/less4j/wiki/_pages). 

For those interested about project internals, architecture and comments handling are described in a [blog post] (http://meri-stuff.blogspot.sk/2012/09/tackling-comments-in-antlr-compiler.html). The blog post captures our ideas at the time of its writing, so current implementation may be a bit different.

## Command Line
Less4j can run from [command line](https://github.com/SomMeri/less4j/wiki/Command-Line-Options). Latest versions are shared via [less4j dropbox account](https://www.dropbox.com/sh/zcb8p27db9ou4x1/keQWIZziH8). Shared folder always contains at least two latest versions, but we may remove older ones. 

If you need an old version for some reason, checkout appropriate tag from git and use `mvn package -P standalone` command. The command compiles less4j and all its dependencies into `target/less4j-<version>-shaded.jar` file. 

## Maven
Less4j is [available](http://search.maven.org/#artifactdetails|com.github.sommeri|less4j|0.0.1|jar) in Maven central repository.

Pom.xml dependency:
<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;com.github.sommeri&lt;/groupId&gt;
  &lt;artifactId&gt;less4j&lt;/artifactId&gt;
  &lt;version&gt;0.0.11&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Integration With Wro4j
The easiest way to integrate less4j into Java project is to use [wro4j](http://alexo.github.com/wro4j/) library. More about wro4j can be found either in a [blog post](http://meri-stuff.blogspot.sk/2012/08/wro4j-page-load-optimization-and-lessjs.html) or on wro4j [google code](http://code.google.com/p/wro4j/) page.

## API:
Warning: Project is still in alpha and current API is very temporary. It will change in the future. 

Access the compiler either through the `com.github.less4j.LessCompiler` interface. Its thread safe implementation is `com.github.less4j.core.DefaultLessCompiler`. The interface exposes two methods:
*  `CompilationResult compile(File inputFile)` - compiles a file, 
*  `CompilationResult compile(String lessContent)` - compiles a string.

The first and second method differ in one important point: the second method is unable to handle "@import" statements located in compiled string. Files referenced by the import statement are relative to current file. Compiler invoked through string based compile method is unable to find imported less files and therefore leaves the import statement as it is. Use it with caution.

Return object `CompilationResult` has two methods: 
* `getCss` - returns compiled css,
* `getWarnings` - returns list of compilation warnings or an empty list. 

Each warning is described by an error message and knows both line and character number of the place that caused it.  

<pre><code>// create input file
File inputLessFile = createFile("sampleInput.less", "* { margin: 1 1 1 1; }");

// compile it
LessCompiler compiler = new ThreadUnsafeLessCompiler();
CompilationResult compilationResult = compiler.compile(inputLessFile);

// print results to console
System.out.println(compilationResult.getCss());
for (Problem warning : compilationResult.getWarnings()) {
  System.err.println(format(warning));
}

private static String format(Problem warning) {
  return "WARNING " + warning.getLine() +":" + warning.getCharacter()+ " " + warning.getMessage();
}
</code></pre>

The method may throw `Less4jException`. The exception is checked and can return list of all found compilation errors. In addition, compilation of some syntactically incorrect inputs may still lead to some output or produce a list of warnings. If this is the case, produced css is most likely invalid and the list of warnings incomplete. Even if they are invalid, they still can occasionally help to find errors in the input and the exception provides access to them. 

* `List<Problem> getErrors` - list of all found compilation errors.
* `CompilationResult getPartialResult()` -  css and list of warnings produced despite compilation errors. There is no guarantee on what exactly will be returned. Use with caution.  

## Links:
*  [http://www.w3.org/Style/CSS/specs.en.html]
*  [http://www.w3.org/Style/CSS/Test/CSS3/Selectors/current/]
*  [http://www.w3.org/TR/css3-selectors/] 
*  [http://www.w3.org/wiki/CSS3/Selectors]
*  [http://www.w3.org/TR/CSS2/]
*  [http://www.w3.org/TR/CSS2/syndata.html#numbers]
*  [http://www.w3.org/TR/2012/WD-css3-fonts-20120823/]
*  [http://www.w3.org/TR/CSS21/fonts.html]
*  Comparison of less and sass: [https://gist.github.com/674726]


