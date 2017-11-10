import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class SpeedOffence {
	// offence percentage caused by speeding by drivers
		
	  // Mapper class
		
		public static class SpeedDataMapper extends Mapper<LongWritable, Text, Text, IntWritable>
		{
			public void map(LongWritable key, Text value, Context context)
			{
				try
				{
					String str[] = value.toString().split(",");
					String vehicleNo = str[0];
					
					Integer speed = Integer.parseInt(str[1]);
					
					context.write(new Text(vehicleNo), new IntWritable(speed));
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
		
		
		// Reducer class
		
		public static class SpeedDataReducer extends Reducer<Text, IntWritable, Text, FloatWritable>
		{
			public void reduce(Text key, Iterable<IntWritable> values, Context context)
			{
				try {
				int totalcount=0;
				int offencecount = 0;
				for(IntWritable val : values)
				{
					
					if(val.get() > 65)
					{
						offencecount++;
					}
					totalcount++;
				}
				
				Float totalrecords = (float) totalcount;
				Float offencerecords = (float) offencecount;
				
				Float offencepercent =  (offencerecords)/(totalrecords)*100;
				
				context.write(new Text(key), new FloatWritable(offencepercent));
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
		
		// main method
		public static void main(String args[]) throws Exception
		{
			Configuration conf = new Configuration();
			Job job = Job.getInstance(conf, "Speeding offence percentage");
			
			job.setJarByClass(SpeedOffence.class);
			
			job.setMapperClass(SpeedDataMapper.class);
			job.setReducerClass(SpeedDataReducer.class);
			
			job.setMapOutputKeyClass(Text.class);
			job.setMapOutputValueClass(IntWritable.class);
			
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(FloatWritable.class);
			
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
			System.exit(job.waitForCompletion(true) ? 0 : 1);
			
		}
		

}
