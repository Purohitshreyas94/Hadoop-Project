

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class PartitionByAge {
     //Mapper Class
	public static class PartitionMapClass extends Mapper<LongWritable,Text,Text,Text>
     {
    	 public void map(LongWritable key, Text value, Context context)throws IOException,InterruptedException
    	 {
    		 String[] str = value.toString().split(",");
    		 String gender=str[3];
    		 //String name= records[1];
    		 //String age= records[2];
    		 //String salary=records[4];
    		 //String pvalue = name+ ',' + age + ',' + salary;
    		 context.write(new Text(gender), new Text(value));
    	 }
     }

	//partitioner class
 public static class AgePartitioner extends Partitioner<Text,Text>
 {
	 public int getPartition(Text key, Text value, int numReduceTasks)
	 {
		 String[] str= value.toString().split(",");
		 int age = Integer.parseInt(str[2]);
		 
		 if(age<=20)
		 {
			 return 0 % numReduceTasks; 
		 }
		 
		 else if(age >20 && age<=30)
		 {
			 return 1 % numReduceTasks;
		 }
		 
		 else
		 {
			 return 2 % numReduceTasks;
		 }
	 }
 }
 
 //Reducer class
 
 public static class PartitionReduceClass extends Reducer<Text,Text,Text,IntWritable>
 {
	 public int max=0;
	 private Text outputkey= new Text();
	 public void reduce(Text key, Iterable <Text> values, Context context)throws IOException,InterruptedException
	 {
		 max = 0;
		 for (Text val : values)
		 {
			String [] str = val.toString().split(",");
			if(Integer.parseInt(str[4])>max)
			{
				max=Integer.parseInt(str[4]);
				String mykey = str[3] + ',' + str[1] + ',' + str[2];
				outputkey.set(mykey);
			}
		 }
	  context.write(outputkey, new IntWritable(max));
	 }
	 
 }
 
 //Main method
 public static void main(String[] args)throws Exception{
 {
	 Configuration conf= new Configuration();
	   
	   Job job = Job.getInstance(conf, "Top Salaried Employees");
	   job.setJarByClass(PartitionByAge.class);
	   
	   job.setMapperClass(PartitionMapClass.class);
	   job.setMapOutputKeyClass(Text.class);
	   job.setMapOutputValueClass(Text.class);
	   
	   
	    job.setPartitionerClass(AgePartitioner.class);
	    job.setReducerClass(PartitionReduceClass.class);
	    job.setNumReduceTasks(3);
	    
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
 	  
    
    } 
 }
}

