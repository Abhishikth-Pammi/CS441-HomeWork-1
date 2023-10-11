import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import org.apache.hadoop.io.*

import util.*
import org.apache.hadoop.mapreduce.*
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import scala.collection.JavaConverters._



import java.io.*
import java.lang
import java.util.ArrayList

case class NodeObject(
                       id: Int,
                       children: Int,
                       props: Int,
                       currentDepth: Int = 1,
                       propValueRange: Int,
                       maxDepth: Int,
                       maxBranchingFactor: Int,
                       maxProperties: Int,
                       storedValue: Double
                     )


object SerializationUtil {
  // Custom encoder and decoder for ArrayList
  implicit def encodeArrayList[T: Encoder]: Encoder[ArrayList[T]] = Encoder.encodeList[T].contramap(_.toArray.toList.asInstanceOf[List[T]])
  implicit def decodeArrayList[T: Decoder]: Decoder[ArrayList[T]] = Decoder.decodeList[T].map(list => {
    val arrayList = new ArrayList[T]()
    list.foreach(arrayList.add)
    arrayList
  })

  def serializeToText(data: (Array[ArrayList[NodeObject]], Array[ArrayList[NodeObject]])): String = {
    data.asJson.noSpaces
  }

  def deserializeFromText(text: String): Either[Error, (Array[List[NodeObject]], Array[List[NodeObject]])] = {
    decode[(Array[List[NodeObject]], Array[List[NodeObject]])](text)
  }
}


class SimRankMapper extends Mapper[LongWritable, Text, Text, DoubleWritable] {

  def euclideanDistance(node1: NodeObject, node2: NodeObject): Double = {
    val attributes1 = Seq(
      node1.id.toDouble,
      node1.children.toDouble,
      node1.props.toDouble,
      node1.currentDepth.toDouble,
      node1.propValueRange.toDouble,
      node1.maxDepth.toDouble,
      node1.maxBranchingFactor.toDouble,
      node1.maxProperties.toDouble,
      node1.storedValue
    )
    val attributes2 = Seq(
      node2.id.toDouble,
      node2.children.toDouble,
      node2.props.toDouble,
      node2.currentDepth.toDouble,
      node2.propValueRange.toDouble,
      node2.maxDepth.toDouble,
      node2.maxBranchingFactor.toDouble,
      node2.maxProperties.toDouble,
      node2.storedValue
    )

    val squaredDifferences = attributes1.zip(attributes2).map { case (a, b) =>
      val diff = a - b
      diff * diff
    }

    val distance = math.sqrt(squaredDifferences.sum)
    1 / (1 + distance)
  }

  def simrank(node1: NodeObject, node2: NodeObject): Double = {
    euclideanDistance(node1, node2)
  }

  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, DoubleWritable]#Context): Unit = {
    // Deserialize value to get the arrays
    val deserializedData = SerializationUtil.deserializeFromText(value.toString)

    deserializedData match {
      case Right((shard1, shard2)) =>
        for (list1 <- shard1; node1 <- list1) {
          for (list2 <- shard2; node2 <- list2) {
            val similarity = simrank(node1, node2)
            context.write(new Text(s"(${node1.id}, ${node2.id})"), new DoubleWritable(similarity))
          }
        }
      case Left(error) =>
        // Handle error (you might log it, or write it to a special error output)
        System.err.println(s"Failed to deserialize input: $error")
    }
  }
}

class SimRankReducer extends Reducer[Text, DoubleWritable, Text, DoubleWritable] {

  val THRESHOLD = 0.005654533979115721

  override def reduce(key: Text, values: java.lang.Iterable[DoubleWritable], context: Reducer[Text, DoubleWritable, Text, DoubleWritable]#Context): Unit = {

    // Extract the nodeIds from the key (which is in the format (node1, node2))
    val Array(node1, node2) = key.toString.drop(1).dropRight(1).split(", ").map(_.trim)

    // Go through each similarity value for the key and check if it's above the threshold.
    for (similarity <- values.asScala) {
      if (similarity.get() > THRESHOLD) {
        context.write(new Text(s"Similarity between node $node1 and node $node2:"), similarity)
      }
    }
  }
}




object AbhiJob {
  def main(args: Array[String]): Unit = {
    val job = Job.getInstance()
    job.setJarByClass(this.getClass)
    job.setJobName("AbhiJob")

    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))

    job.setMapperClass(classOf[SimRankMapper])
    job.setReducerClass(classOf[SimRankReducer])

    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[DoubleWritable])


    System.exit(if (job.waitForCompletion(true)) 0 else 1)
  }
}
