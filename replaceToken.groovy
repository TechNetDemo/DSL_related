import hudson.model.*; 

//read content from designated file
String target_file = Thread.currentThread().executable.workspace.toString() + "/WEB-INF/web.xml"
String fileContents = new File(target_file).text

println("[before]"); 
println(fileContents);

 //get Jenkins parameters
 def parameters = build?.actions.find{ it instanceof ParametersAction }?.parameters

//replace token foreach
 parameters.each {
  	 println "parameter ${it.name}:" + it.getValue();
         fileContents = fileContents.replaceAll("@@@${it.name}@@@" , it.getValue())
}

println("[after ]")
println(fileContents);

//output file
File output = new File(target_file)
output.write(fileContents)
