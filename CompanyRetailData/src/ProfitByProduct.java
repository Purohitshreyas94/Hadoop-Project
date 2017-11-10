


import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class ProfitByProduct {
     
	//Mapper Class
	public static class ProductProfitMapper extends Mapper<LongWritable,Text,Text,LongWritable>
	{
		public void map(LongWritable key, Text value, Context context)throws IOException,InterruptedException
	      {	    	  
	         
	            String[] str = value.toString().split(";");
	            String pid = str[5];
	            long Cost = Integer.parseInt(str[7]);
	            long Sale = Integer.parseInt(str[8]);
	            
	            long profit = Sale - Cost;
	            context.write(new Text(pid),new LongWritable(profit));
	         
	      }
	}
	
	//Reducer Class
	public static class ProductProfitReducer extends Reducer<Text,LongWritable,Text,LongWritable>
	{
		private LongWritable result = new LongWritable();
		
  public void reduce(Text key, Iterable<LongWritable> values,Context context) throws IOException, InterruptedException
		 {
			 long sum = 0;
				
	         for (LongWritable val : values)
	         {       	
	        	sum += val.get();      
	         }
	         
	      result.set(sum);		      
	      context.write(key, result);
		 }
	
	}
	
	
	//Main Method
	public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	    Job job = Job.getInstance(conf, "Gross Profit By ProductId");
	    
	    job.setJarByClass(ProfitByProduct.class);
	    job.setMapperClass(ProductProfitMapper.class);
	    job.setReducerClass(ProductProfitReducer.class);
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(LongWritable.class);
	    
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  }

}
