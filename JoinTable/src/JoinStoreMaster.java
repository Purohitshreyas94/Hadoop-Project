
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class JoinStoreMaster {

	
	public static class StoreMapper extends Mapper<LongWritable, Text, Text, IntWritable>
	{

		private Map<String, String> map =  new HashMap<String, String>();
		
		
		private Text outputkey = new Text();
		//private Text outputvalue = new Text();
		
		@Override
		protected void setup(Context context) throws IOException,InterruptedException {
			//super.setup(context);
			URI[] files = context.getCacheFiles();
			 
			 Path p1 = new Path(files[0]);
			 
			 FileSystem  fs= FileSystem.get(context.getConfiguration());
			 
			 BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(p1)));
			 
			 String line = br.readLine();
			 
			 while(line != null)
			 {
				 String[] token = line.split(",");
				 String store_id = token[0];
				 String state = token[2];
				 
				 map.put(store_id, state);
				 line = br.readLine();
			 }
			 br.close();
			
		}

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String row = value.toString();
			String[] token = row.split(",");
			String store_id = token[0];
			String state = map.get(store_id);
			String product_id = token[1];
			
			String myKey = state+","+product_id;
			
			int qty = Integer.parseInt(token[2]);
			
			outputkey.set(myKey);
			//outputvalue.set(row);
			
			context.write(outputkey, new IntWritable(qty));
		}
		
	}
	
	public static class StatePartitioner extends Partitioner<Text, IntWritable>
	{

		@Override
		public int getPartition(Text key, IntWritable value, int numReduceTasks) {
			
			String[] str = key.toString().split(",");
			String state = str[0];
			if(state.contains("MAH"))
			{
				return 0 % numReduceTasks;
			}
			else
			{
				return 1 % numReduceTasks;
			}
			
			
		}
		
	}
	
	public static class StoreReducer extends Reducer<Text, IntWritable, Text, IntWritable>
	{

		IntWritable result = new IntWritable();
		
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException {
			
			int sum = 0;
			
			for(IntWritable val : values)
			{
				/*String[] str = val.toString().split(",");
				int qty = Integer.parseInt(str[2]);
				if (qty > sum)
				{
					sum += qty;
				}*/
				sum += val.get();
			}
			
			result.set(sum);
			context.write(key, result);
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();
		conf.set("mapreduce.output.textoutputformat.separator", ",");
		Job job = Job.getInstance(conf, "State wise Product sales");
		job.setJobName("Map Side Join");
		job.addCacheFile(new Path(args[1]).toUri());
		job.setJarByClass(JoinStoreMaster.class);
		job.setMapperClass(StoreMapper.class);
		job.setPartitionerClass(StatePartitioner.class);
		job.setReducerClass(StoreReducer.class);
		job.setNumReduceTasks(2);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		
		System.exit(job.waitForCompletion(true) ? 0: 1);
	}

}