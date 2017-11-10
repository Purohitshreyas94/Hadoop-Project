

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class HighestTransaction {

	//Mapper Class
	
	public static class HighestTransMapper extends Mapper<LongWritable,Text,Text,Text>
	{
		public void map(LongWritable key, Text value, Context context)throws IOException,InterruptedException
		{
		    	String[] records = value.toString().split(";");
		    	String tkey = "all";
		    	String datetime = records[0];
		    	String custid = records[1];
		    	String sales = records[8];
		    	String Value = datetime+ "," +custid+ "," +sales;
		    	context.write(new Text(tkey), new Text(Value));
		    			
		}
	}
	
	//Reducer Class
	public static class HighestTransReducer extends Reducer<Text,Text,NullWritable,Text>
	{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
		{
			long MAX=0;
			String custid="";
			String datetime="";
			
			for(Text val : values)
			{
				String[] records = val.toString().split(",");
				long transaction = Long.parseLong(records[2]);
				String date = records[1];
				String custid1= records[0];
				
				if(transaction > MAX)
				{
					custid=custid1;
					datetime=date;
					MAX=transaction;
				}
			}
			
		  String result= custid+","+datetime+"," +MAX;
		
		  context.write(NullWritable.get(),new Text(result));
		}
	}
	
	public static void main(String[] args)throws Exception
	{
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Highest Transaction");
		
		job.setJarByClass(HighestTransaction.class);
		job.setMapperClass(HighestTransMapper.class);
		job.setReducerClass(HighestTransReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		
		 job.setOutputKeyClass(NullWritable.class);
		 job.setOutputValueClass(Text.class);
		 
		 FileInputFormat.addInputPath(job, new Path(args[0]));
		 FileOutputFormat.setOutputPath(job, new Path(args[1]));
		    
		 System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}
