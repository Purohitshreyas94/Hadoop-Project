
// Program to find Total Sales and Purchases for each Product by their ID.
// Input Files:- purchase.txt, sales.txt

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;




public class ProductSalesJoin {
	
	public static class PurchaseMapper extends Mapper<LongWritable, Text, Text, Text> {

 public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
 {
	String record = value.toString();
	String[] parts = record.split(",");
	context.write(new Text(parts[0]), new Text("purchase\t" + parts[1]));
}
}

	
public static class SalesMapper extends Mapper<LongWritable, Text, Text, Text> {

public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException 
{
	String record = value.toString();
	String[] parts = record.split(",");
	context.write(new Text(parts[0]), new Text("sales\t" + parts[1]));
}
}

public static class ReduceJoinReducer extends Reducer<Text, Text, Text, Text> 
{
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException 
	{
    
	   int tp = 0;
	   int ts = 0;
	    
	    for (Text t : values) 
	    {
	    	String parts[] = t.toString().split("\t");
	    	if (parts[0].equals("purchase")) {
				
				tp += Integer.parseInt(parts[1]);
	    	}
	    	
	    	else if(parts[0].equals("sales")){
	    		
	    		ts += Integer.parseInt(parts[1]);
	    	}
	    }
	    
	    String result = tp + "," +ts;
	    context.write(key, new Text(result));
		
	}



}

public static void main(String[] args) throws Exception {
	
	Configuration conf = new Configuration();
	Job job = Job.getInstance(conf);
    job.setJarByClass(ProductSalesJoin.class);
    job.setJobName("Product Sales Join");
	job.setReducerClass(ReduceJoinReducer.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(Text.class);
	//job.setNumReduceTasks(0);
	MultipleInputs.addInputPath(job, new Path(args[0]),TextInputFormat.class, PurchaseMapper.class);
	MultipleInputs.addInputPath(job, new Path(args[1]),TextInputFormat.class, SalesMapper.class);
	
	Path outputPath = new Path(args[2]);
	FileOutputFormat.setOutputPath(job, outputPath);
	//outputPath.getFileSystem(conf).delete(outputPath);
	
	System.exit(job.waitForCompletion(true) ? 0 : 1);
}
}
