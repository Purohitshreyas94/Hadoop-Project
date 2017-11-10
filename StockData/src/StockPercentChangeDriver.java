import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class StockPercentChangeDriver {
	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.out.printf("Usage: StockPercentChangeDriver <input dir> <output dir>\n");
			System.exit(-1);
		}

		
		Configuration conf = new Configuration();
		//conf.set("","")
		Job job = Job.getInstance(conf, "NYSE_Computation");

		job.setJarByClass(StockPercentChangeDriver.class);

		//job.setJobName("NYSE Stock");

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.setMapperClass(NYSEMapper.class);
		//job.setCombinerClass(NYSEReducer.class);
		job.setReducerClass(NYSEReducer.class);
		//job.setNumReduceTasks(0);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(FloatWritable.class);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
	}

}