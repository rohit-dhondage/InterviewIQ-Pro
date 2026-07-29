package com.example.Interview.rag;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanChecker implements CommandLineRunner {


    private final ApplicationContext context;

    public BeanChecker(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {

        String[] beans = context.getBeanNamesForType(EmbeddingModel.class);

        System.out.println("========== EMBEDDING BEANS ==========");

        for (String bean : beans) {
            System.out.println(bean + " -> " + context.getBean(bean).getClass().getName());
        }

        System.out.println("=====================================");
    }
}