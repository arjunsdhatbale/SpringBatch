package com.main.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.main.model.Product;

@Configuration
public class BatchConfig {

	@Bean
	public ApplicationRunner applicationRunner(JobLauncher jobLauncher, Job job) {
	    return args -> {
	        try {
	            jobLauncher.run(job, new JobParameters());
	        } catch (JobExecutionAlreadyRunningException e) {
	            System.out.println("The job is already running.");
	        } catch (Exception e) {
	            e.printStackTrace();  // Handle other exceptions
	        }
	    };
	}

	
	
	@Bean
	public Job jobBean(JobRepository jobRepository, JobCompletionNotificationImpl listener, Step steps) {

		return new JobBuilder("job", jobRepository)
				.listener(listener)
				.start(steps)
				.build();
	}

	@Bean
	public Step steps(JobRepository jobRepository,
			DataSourceTransactionManager transactionManager,
			FlatFileItemReader<Product> reader, 
			ItemProcessor<Product, Product> processor,
			ItemWriter<Product> writer) {

		return new StepBuilder("jobStep", jobRepository)
				.<Product,Product>chunk(5, transactionManager)
				.reader(reader)
				.processor(processor)
				.writer(writer)
				.build();

	}

	
	// reader

	@Bean
	public FlatFileItemReader<Product> reader() {

		FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
		reader.setName("itemReader");
		reader.setResource(new ClassPathResource("data.csv"));
		
		reader.setLinesToSkip(1);
		
		DelimitedLineTokenizer tokenizer  = new DelimitedLineTokenizer();
		
		tokenizer.setNames( "productId", "title", "description", "price", "discount" );
		
		BeanWrapperFieldSetMapper<Product> filedSetMapper = new BeanWrapperFieldSetMapper<>(); 
		
		filedSetMapper.setTargetType(Product.class);
		
		DefaultLineMapper<Product> lineMapper  = new DefaultLineMapper<>();
		
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(filedSetMapper);
		
		reader.setLineMapper(lineMapper);
		
		return reader; 
		
//		return new FlatFileItemReader<Product>()
//				.name("itemReader")
//				.resource(new ClassPathResource("data.csv"))
//				.delimited().names("productId", "title", "description", "price", "discount").targetType(Product.class)
//				.build();
	}

// processor

	@Bean
	public ItemProcessor<Product, Product> itemProcessor() {

		return new CustomItemProcessor();

	}

// writer

	@Bean
	public ItemWriter<Product> itemWriter(DataSource dataSource) {
	    JdbcBatchItemWriter<Product> writer = new JdbcBatchItemWriter<>();
	    
	    writer.setSql("INSERT INTO products (productId, title, description, price, discount, discountedPrice) " +
	                  "VALUES (:productId, :title, :description, :price, :discount, :discountedPrice)");

	    writer.setDataSource(dataSource);
  // , discountedPrice  
	    // , :discountedPrice
	    // Use BeanPropertyItemSqlParameterSourceProvider to map properties of the Product bean to SQL parameters
	    writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

	    return writer;
	}


}
