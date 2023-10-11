# NetGameSim-Demo

This project deals with generating graphs and finding the similarity between the original and the perturbed graphs therby showing the similarity of one node with respect to the other.
I will create a distributed program for parallel processing of the large graphs. The final part is to compare with the YAML file and find the precision of the approach of computing differences between the original and the perturbed graph.

## Youtube Demo Link :
https://youtu.be/gX9ob-mGaIE
## Prerequisites
https://github.com/Abhishikth-Pammi/CS441-Homework1-sub.git (Use this sub project which generates the serialized text file which is an input for map reduce).

Before you begin, ensure you have met the following requirements:

- **Java:** The project typically requires Java. You can check if it's installed using:
  java -version

mark

- **SBT (Scala Build Tool):** This is essential to build and package Scala projects. If it's not already installed, follow the installation guidelines [here](https://www.scala-sbt.org/download.html).

## Building the Project using `sbt assembly`

1. **Clone the repository:**

git clone https://github.com/Abhishikth-Pammi/CS441-HomeWork-1.git
cd CS441-HomeWork-1

2. **Clean the project (Optional):**

It's generally a good practice to clean your project before assembling it.

sbt clean

3. **Compile the project:**

Ensure there are no compilation errors.

sbt compile

4**Run the assembly command:**

sbt assembly

Upon successful execution, this command creates a single assembly JAR file under the `target/scala-x.x.x/` directory. This JAR will contain your project's compiled class files as well as its dependencies.

5**Running the assembled JAR (Optional):**

If you want to run the application after assembling:

java -jar target/scala-x.x.x/CS441-HomeWork-1-assembly-x.x.x.jar
