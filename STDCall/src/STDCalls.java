

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class STDCalls {
public static class STDMappClass extends Mapper<LongWritable,Text,Text,IntWritable>
{
    Text phoneNumber = new Text();
    IntWritable durationMinutes = new IntWritable();
    
    public void map(LongWritable key, Text value, Context context)throws IOException,InterruptedException
    {
    	String[] records = value.toString().split(",");
    	if(records[4].equals("1")){
    		phoneNumber.set(records[0]); //set the phonenumber who have flag value 1
    	
    	String callStartTime=records[2];
    	String callEndTime=records[3];
    	
    	long duration = toMillis(callEndTime) - toMillis(callStartTime);
    	durationMinutes.set((int)(duration/(1000 * 60))); //convert milliseconds into seconds and then converts seconds into minutes.
    	
    	context.write(phoneNumber,durationMinutes );
    	}
    	
   }
    
    private long toMillis(String date){
    	 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	 Date dateFrm = null;;
    	 try {
             dateFrm = format.parse(date);

         } catch (ParseException e) {

             e.printStackTrace();
        }
         return dateFrm.getTime();
     }

    }
    
   public static class STDReduceClass extends Reducer<Text,IntWritable,Text,IntWritable>
   {
	   private IntWritable result = new IntWritable();
	   public void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException, InterruptedException {
           long sum = 0;
           for (IntWritable val : values) {
               sum += val.get();
           }
           if (sum >= 60) {
           	result.set((int) sum);
           	context.write(key, result);
           }
	    }
   }
   
   
   public static void main(String[] args)throws Exception{
	   Configuration conf= new Configuration();
	   conf.set("mapreduce.output.textoutputformat.separator",",");
	   Job job = Job.getInstance(conf, "STD Calls");
	   job.setJarByClass(STDCalls.class);
	   
	   job.setMapperClass(STDMappClass.class);
	   job.setReducerClass(STDReduceClass.class);
	   
	   job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setOutputFormatClass(TextOutputFormat.class);
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  }
   
}
   


