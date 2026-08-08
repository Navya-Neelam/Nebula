package com.nebula.auth.config;

import com.nebula.auth.model.Course;
import com.nebula.auth.model.User;
import com.nebula.auth.repository.CourseRepository;
import com.nebula.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(CourseRepository courseRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed initial users if none exist
        if (userRepository.count() == 0) {
            User admin = new User("System Admin", "admin@nebula.com", passwordEncoder.encode("Password123"), LocalDateTime.now());
            admin.setRole("ADMIN");
            admin.setVerified(true);
            admin.setActive(true);
            admin.setFirstName("System");
            admin.setLastName("Admin");
            userRepository.save(admin);

            User instructor = new User("Dr. Clara Sterling", "clara@nebula.com", passwordEncoder.encode("Password123"), LocalDateTime.now());
            instructor.setRole("INSTRUCTOR");
            instructor.setVerified(true);
            instructor.setActive(true);
            instructor.setFirstName("Clara");
            instructor.setLastName("Sterling");
            User savedInstructor = userRepository.save(instructor);

            User student = new User("Jane Student", "student@nebula.com", passwordEncoder.encode("Password123"), LocalDateTime.now());
            student.setRole("STUDENT");
            student.setVerified(true);
            student.setActive(true);
            student.setFirstName("Jane");
            student.setLastName("Student");
            userRepository.save(student);
        }

        // Clear and re-seed to ensure all courses contain the new price field and backend courses
        courseRepository.deleteAll();

        List<Course> courses = Arrays.asList(
            new Course(
                "Introduction to Generative AI",
                "Learn the fundamentals of Generative AI, Large Language Models (LLMs), and prompt engineering.",
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "6 hours",
                4.8,
                "Dr. Clara Sterling",
                "This course provides a comprehensive introduction to Generative AI. You will learn the history of transformer models, prompt engineering patterns, fine-tuning techniques, and how to build applications using LLM APIs. Ideal for beginners and professionals wanting to stay ahead in tech.",
                49.99
            ),
            new Course(
                "Advanced Prompt Engineering",
                "Master complex prompt engineering frameworks and build multi-agent LLM systems.",
                "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "8 hours",
                4.9,
                "Alex Rivera",
                "Unlock the true potential of AI with advanced techniques like Chain-of-Thought, ReAct framing, and Directional Stimulus prompting. You will construct autonomous agents using LangChain and learn practical security measures against prompt injection.",
                79.99
            ),
            new Course(
                "Data Science & Machine Learning Bootcamp",
                "Go from zero to hero in Python, Pandas, NumPy, Scikit-Learn, and statistics.",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "40 hours",
                4.7,
                "Prof. Marcus Vance",
                "A complete bootcamp covering the full data science lifecycle. Learn data visualization, exploratory analysis, linear regression, decision trees, random forests, and unsupervised learning algorithms. Includes hands-on projects with real-world datasets.",
                149.99
            ),
            new Course(
                "Deep Learning & Neural Networks",
                "Understand CNNs, RNNs, and Transformers using PyTorch and TensorFlow.",
                "https://images.unsplash.com/photo-1527474305487-b87b222841cc?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "25 hours",
                4.8,
                "Dr. Sophia Chen",
                "Delve deep into neural network architectures. Learn backpropagation, activation functions, convolutional layers for computer vision, recurrent structures for sequence processing, and the revolutionary attention mechanism that powers today's LLMs.",
                119.99
            ),
            new Course(
                "Introduction to SQL & Database Design",
                "Master PostgreSQL, relational database architecture, and complex subqueries.",
                "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "14 hours",
                4.7,
                "Colt Steele",
                "Learn relational design principles, indexing optimizations, and query writing. Write complex inner/outer joins, aggregate analytical operations, subqueries, trigger statements, and window mathematical functions.",
                39.99
            ),
            new Course(
                "Angular 18 Complete Guide",
                "Master standalone components, signals, routing, and RxJS in modern Angular.",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "30 hours",
                4.8,
                "Maximilian Schwarz",
                "Build performant enterprise web applications using modern Angular. Learn the new Signals state management model, standalone component architectures, advanced routing strategies, and complex RxJS data streams.",
                99.99
            ),
            new Course(
                "Full-Stack Spring Boot & React",
                "Connect Spring Boot REST APIs with a modern React frontend using Spring Security.",
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "35 hours",
                4.9,
                "John Thompson",
                "Develop a complete production-ready application. Build secure Spring Boot REST endpoints with JWT validation, connect to a PostgreSQL database, and manage front-end authentication states seamlessly using React Context and Axios.",
                129.99
            ),
            new Course(
                "Figma UI/UX Design Essentials",
                "Learn user research, wireframing, prototyping, and design systems in Figma.",
                "https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "20 hours",
                4.8,
                "Dan Walter",
                "Design high-fidelity interactive user interfaces and build robust user experience maps. Create nested layout components, configure responsive grids, run micro-interactive prototyping flows, and share design handoffs with developers.",
                59.99
            ),
            new Course(
                "Modern JavaScript & TypeScript Masterclass",
                "Master ES6+, asynchronous programming, closures, and TypeScript compiler basics.",
                "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "22 hours",
                4.8,
                "Sarah Drasner",
                "Become a JavaScript expert. Learn advanced concepts like closures, async-await architectures, execution contexts, variable scopes, event loops, and type-safety modeling using TypeScript. Highly recommended for all developers.",
                69.99
            ),
            new Course(
                "Spring Boot Microservices Masterclass",
                "Build scalable backend systems using Spring Cloud, Eureka, Gateway, and Config Server.",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "28 hours",
                4.9,
                "Dr. John Doe",
                "Master microservices architecture using Spring Boot and Spring Cloud. Learn service discovery with Eureka, API Gateway routing, centralized configuration, circuit breakers with Resilience4j, and event-driven communication using Kafka.",
                119.99
            ),
            new Course(
                "Node.js Advanced API Design",
                "Learn scalable Express APIs, MongoDB, Redis caching, and WebSocket integrations.",
                "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "24 hours",
                4.8,
                "Sarah Connor",
                "Go deep into Node.js. Build high-performance REST and GraphQL APIs, implement JWT authentication, secure endpoints with rate limiting, cache queries with Redis, and implement real-time communication using WebSockets.",
                89.99
            ),
            new Course(
                "Go (Golang) Backend Essentials",
                "Build highly concurrent microservices with Fiber, Gin, GORM, and Docker.",
                "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "20 hours",
                4.7,
                "Ken Thompson",
                "Learn Google's Go language for backend systems. Harness Go routines and channels for high concurrency, write clean APIs with the Fiber framework, interface with databases using GORM, and write unit and integration tests.",
                99.99
            ),
            new Course(
                "Python Django & FastAPI Bootcamp",
                "Build reactive backends with Django ORM, REST Framework, and asynchronous FastAPI.",
                "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "32 hours",
                4.8,
                "Guido van Rossum",
                "A comprehensive backend bootcamp using Python. Build relational CRUD systems using Django ORM and Django REST Framework, and develop blazingly fast asynchronous APIs using FastAPI and Pydantic validation.",
                109.99
            ),
            new Course(
                "Docker & Kubernetes for Developers",
                "Learn containerization, scaling, orchestration, and local microservice deployment.",
                "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "15 hours",
                4.6,
                "Nigel Poulton",
                "Understand how to package applications into lightweight containers, manage multi-container systems using Docker Compose, and orchestrate container lifecycles in scale using Kubernetes clusters and Helm charts.",
                59.99
            ),
            new Course(
                "AWS Certified Solutions Architect",
                "Pass the SAA-C03 exam and learn AWS EC2, S3, RDS, Lambda, and VPC inside out.",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "28 hours",
                4.8,
                "Stephane Maarek",
                "Fully prepare for the AWS Solutions Architect Associate exam. Learn cloud architecture best practices, compute hosting options, scalable database configurations, serverless execution models, and secure private virtual networking.",
                129.99
            ),
            new Course(
                "Cybersecurity Fundamentals",
                "Learn cryptography, network defense, threat hunting, and social engineering.",
                "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "18 hours",
                4.7,
                "Sarah Jenkins",
                "Start your journey in ethical hacking and defense. Understand public-key cryptography, firewalls, intrusion detection systems, malware analysis tools, and the human psychological triggers exploited by social engineering campaigns.",
                69.99
            ),
            new Course(
                "Ethical Hacking: Web Penetration Testing",
                "Discover SQL injection, XSS, CSRF, and session hijacking using Kali Linux.",
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "22 hours",
                4.9,
                "Daniel Lowrie",
                "Perform comprehensive web app security audits. Learn to identify OWASP Top 10 vulnerabilities like SQL injection, cross-site scripting (XSS), cross-site request forgery (CSRF), and insecure direct object references using professional pentesting tools.",
                89.99
            ),
            new Course(
                "iOS 18 Swift & SwiftUI Bootcamp",
                "Build beautiful iOS apps using Swift, SwiftUI, SwiftData, and CoreAnimation.",
                "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800&auto=format&fit=crop&q=60",
                "Mobile Development",
                "32 hours",
                4.8,
                "Angela Yu",
                "Master Apple's modern declarative UI framework. Build complex user interfaces, save persistent local data with SwiftData, integrate native maps, configure notification systems, and prepare your apps for the App Store.",
                99.99
            ),
            new Course(
                "Android 15 Jetpack Compose Guide",
                "Learn Kotlin, Jetpack Compose UI, Clean Architecture, and Coroutines.",
                "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=800&auto=format&fit=crop&q=60",
                "Mobile Development",
                "28 hours",
                4.7,
                "Philipp Lackner",
                "Build native Android applications with Kotlin and modern reactive UI tools. Explore MVVM/MVI architecture, state flows, reactive Compose layouts, work manager, local database access via Room, and test-driven development integrations.",
                89.99
            ),
            new Course(
                "Digital Marketing Masters Class",
                "Master Google Ads, SEO, content strategy, email marketing, and analytics.",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop&q=60",
                "Digital Marketing",
                "12 hours",
                4.6,
                "Ryan Deiss",
                "A comprehensive masterclass on driving web traffic and converting leads. Learn to rank websites using search engine optimization (SEO), construct effective search ad campaigns, write email copy, and read analytics data dashboards.",
                39.99
            ),
            new Course(
                "SEO Strategy & Copywriting",
                "Learn keyword research, on-page optimization, and high-converting writing.",
                "https://images.unsplash.com/photo-1432888622747-4eb9a8efeb07?w=800&auto=format&fit=crop&q=60",
                "Digital Marketing",
                "10 hours",
                4.5,
                "Brian Dean",
                "Boost organic search engine rankings and visitor retention. Learn to perform search intent keyword research, write titles that get clicks, structure layout headings, and build natural backlink relationships.",
                29.99
            ),
            // NEW BOOMING COURSES
            new Course(
                "Autonomous AI Agents & Multi-Agent Frameworks",
                "Build autonomous agent networks with AutoGen, CrewAI, and LangGraph.",
                "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "14 hours",
                4.9,
                "Alex Rivera",
                "Architect state-of-the-art multi-agent AI applications. Learn how autonomous agents plan tasks, tool usage, memory integration, and agent orchestration with LangGraph and CrewAI.",
                89.99
            ),
            new Course(
                "LLM Fine-Tuning & Quantization Masterclass",
                "Learn LoRA, QLoRA, HuggingFace Transformers, and vLLM deployment.",
                "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "18 hours",
                4.9,
                "Dr. Clara Sterling",
                "Fine-tune open-source models like Llama 3 and Mistral on domain datasets using Parameter-Efficient Fine-Tuning (PEFT), LoRA, and QLoRA. Deploy quantized models with vLLM for low-latency inferencing.",
                109.99
            ),
            new Course(
                "RAG Engineering & Vector Databases",
                "Build production Retrieval-Augmented Generation systems with Pinecone, Qdrant, and LlamaIndex.",
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "12 hours",
                4.8,
                "Dr. Clara Sterling",
                "Master RAG architectures. Learn document chunking strategies, embedding models, vector similarity indexing with Pinecone and Qdrant, hybrid search, re-ranking, and evaluate RAG accuracy using Ragas.",
                79.99
            ),
            new Course(
                "Multimodal AI & Vision Language Models",
                "Harness GPT-4 Vision, Claude 3, and Whisper for multimodal AI products.",
                "https://images.unsplash.com/photo-1507146426996-ef05306b995a?w=800&auto=format&fit=crop&q=60",
                "Generative AI",
                "16 hours",
                4.8,
                "Sophia Chen",
                "Build applications that see, hear, and converse. Integrate vision language models, automatic speech recognition with OpenAI Whisper, and generative image/video synthesis APIs.",
                94.99
            ),
            new Course(
                "Data Engineering & Analytics with Snowflake & dbt",
                "Build modern cloud data warehouses and ETL pipelines with dbt.",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "22 hours",
                4.8,
                "Prof. Marcus Vance",
                "Master modern data stack engineering. Design dimensional data models in Snowflake, write SQL transformations using dbt, orchestrate data runs with Airflow, and enforce data quality testing.",
                119.99
            ),
            new Course(
                "MLOps & ML Infrastructure with MLflow & Kubeflow",
                "Deploy, monitor, and scale machine learning models in production.",
                "https://images.unsplash.com/photo-1527474305487-b87b222841cc?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "26 hours",
                4.9,
                "Dr. Sophia Chen",
                "Bridge the gap between data science and DevOps. Implement experiment tracking with MLflow, model registries, CI/CD pipelines for ML models, feature stores, and drift monitoring.",
                129.99
            ),
            new Course(
                "Big Data Processing with Apache Spark & Databricks",
                "Process terabytes of streaming and batch data using PySpark.",
                "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&auto=format&fit=crop&q=60",
                "Data Science",
                "30 hours",
                4.7,
                "Prof. Marcus Vance",
                "Learn large-scale distributed data processing. Write PySpark batch transformations, structured streaming queries, Delta Lake lakehouse architectures, and run distributed ML algorithms on Databricks clusters.",
                139.99
            ),
            new Course(
                "Next.js 14 & React 19 Full-Stack Architecture",
                "Master Server Components, Server Actions, App Router, and Prisma.",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "28 hours",
                4.9,
                "Maximilian Schwarz",
                "Build modern, lightning-fast full-stack web apps using Next.js 14 App Router. Learn React Server Components, Server Actions for seamless mutations, Tailwind styling, Auth.js, and Prisma ORM.",
                109.99
            ),
            new Course(
                "Vue 3 & Nuxt 3 Full-Stack Enterprise Guide",
                "Build reactive web apps with Composition API, Pinia, and Nuxt 3.",
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "20 hours",
                4.8,
                "Sarah Drasner",
                "Master Vue 3's script setup and Composition API. Build server-side rendered (SSR) web applications using Nuxt 3, manage global reactive state with Pinia, and deploy to Vercel.",
                89.99
            ),
            new Course(
                "Microfrontends Architecture & Module Federation",
                "Architect scalable enterprise frontend systems using Webpack 5.",
                "https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "18 hours",
                4.7,
                "Dan Walter",
                "Decompose monolithic frontend applications into independently deployable microfrontends using Webpack 5 Module Federation, Single-SPA framework, and shared component design libraries.",
                99.99
            ),
            new Course(
                "Tailwind CSS & Modern UI Animation Masterclass",
                "Create wowed interfaces with Tailwind, Framer Motion, and Three.js.",
                "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=800&auto=format&fit=crop&q=60",
                "Web Development",
                "14 hours",
                4.8,
                "Sarah Drasner",
                "Craft futuristic, interactive web experiences. Learn Tailwind utility techniques, glassmorphism layouts, micro-interactions, layout transitions with Framer Motion, and 3D web graphics with Three.js.",
                49.99
            ),
            new Course(
                "Rust Systems & Backend Engineering",
                "Build high-performance, memory-safe web services using Axum, Actix, and Tokio.",
                "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "26 hours",
                4.9,
                "Ken Thompson",
                "Master Rust's memory safety guarantees, ownership, and borrowing. Build blazingly fast asynchronous HTTP APIs using Axum and Tokio, interface with PostgreSQL via SQLx, and write zero-cost abstractions.",
                119.99
            ),
            new Course(
                "GraphQL API Development with Node & Spring",
                "Design schema-first GraphQL APIs with Federation and Apollo Server.",
                "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "18 hours",
                4.8,
                "Sarah Connor",
                "Learn GraphQL schema definitions, query resolvers, mutations, subscriptions, N+1 query batching with DataLoader, and federated GraphQL microservice architectures.",
                79.99
            ),
            new Course(
                "Event-Driven Microservices with Apache Kafka",
                "Build distributed streaming pipelines and saga pattern architecture.",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "30 hours",
                4.9,
                "Dr. John Doe",
                "Architect resilient event-driven microservices. Master Kafka topics, partitions, consumer groups, Kafka Streams API, Schema Registry with Avro, and implement Saga pattern distributed transactions.",
                129.99
            ),
            new Course(
                "Redis Caching & In-Memory Data Store Architecture",
                "Master Redis enterprise caching, Pub/Sub, and Rate Limiting.",
                "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800&auto=format&fit=crop&q=60",
                "Backend Development",
                "12 hours",
                4.7,
                "Ken Thompson",
                "Optimize application performance with Redis. Implement cache-aside strategies, cache invalidation rules, distributed locking with Redlock, rate limiting, and real-time pub/sub channels.",
                69.99
            ),
            new Course(
                "Terraform & Infrastructure as Code (IaC)",
                "Automate multi-cloud AWS, Azure, and GCP deployments using Terraform.",
                "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "20 hours",
                4.8,
                "Nigel Poulton",
                "Automate cloud provisioning. Write reusable Terraform modules, manage remote state in S3 with DynamoDB locking, manage secrets, and configure CI/CD infrastructure deployments.",
                89.99
            ),
            new Course(
                "Microsoft Azure Solutions Architect Bootcamp",
                "Master Azure Virtual Machines, AKS, Blob Storage, and Entra ID.",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "28 hours",
                4.8,
                "Stephane Maarek",
                "Pass the Azure Solutions Architect Expert exam (AZ-305). Design hybrid cloud compute solutions, configure Azure Kubernetes Service (AKS), manage Azure SQL databases, and enforce IAM via Entra ID.",
                119.99
            ),
            new Course(
                "Google Cloud Platform (GCP) Professional Engineer",
                "Design scalable cloud infrastructure on GCP Compute Engine & GKE.",
                "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "26 hours",
                4.8,
                "Stephane Maarek",
                "Master GCP core services. Build containerized cloud workloads using Google Kubernetes Engine (GKE), BigQuery data warehouses, Cloud Functions serverless execution, and VPC networking.",
                129.99
            ),
            new Course(
                "Kubernetes Security & Hardening (CKS)",
                "Secure production Kubernetes clusters, RBAC, network policies, and Falco.",
                "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
                "Cloud Computing",
                "22 hours",
                4.9,
                "Nigel Poulton",
                "Prepare for the Certified Kubernetes Security Specialist (CKS) exam. Learn cluster hardening, network policies, Pod security standards, container image vulnerability scanning, and runtime threat detection with Falco.",
                99.99
            ),
            new Course(
                "Cloud Security & DevSecOps Engineering",
                "Integrate security into CI/CD pipelines, static analysis, and container scanning.",
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "20 hours",
                4.8,
                "Sarah Jenkins",
                "Automate security checks across software development lifecycles. Implement SAST with SonarQube, DAST scanning with OWASP ZAP, dependency audit tools, and container image signing.",
                99.99
            ),
            new Course(
                "CompTIA Security+ (SY0-701) Certification",
                "Master network security, risk management, incident response, and SOC ops.",
                "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "24 hours",
                4.7,
                "Daniel Lowrie",
                "Comprehensive prep for the Security+ certification exam. Learn core cybersecurity principles, threat actor profiles, cryptographic protocols, security architecture, and SOC incident triage.",
                79.99
            ),
            new Course(
                "API Hacking & OWASP API Security Top 10",
                "Test and secure REST, GraphQL, and gRPC APIs against zero-day exploits.",
                "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
                "Cybersecurity",
                "18 hours",
                4.9,
                "Daniel Lowrie",
                "Identify and fix API security flaws. Master hands-on penetration testing techniques for Broken Object Level Authorization (BOLA), mass assignment, rate limit bypasses, and JWT signature forgery.",
                89.99
            ),
            new Course(
                "Flutter & Dart Cross-Platform Mobile Apps",
                "Build iOS, Android, and Web apps from a single codebase with Riverpod.",
                "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800&auto=format&fit=crop&q=60",
                "Mobile Development",
                "30 hours",
                4.8,
                "Angela Yu",
                "Master Google's Flutter framework. Build responsive widget trees, handle state management using Riverpod, integrate Firebase backend services, and publish apps to Apple App Store & Google Play.",
                89.99
            ),
            new Course(
                "React Native & Expo Full-Stack Mastery",
                "Build cross-platform native mobile apps with Expo Router and NativeWind.",
                "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=800&auto=format&fit=crop&q=60",
                "Mobile Development",
                "26 hours",
                4.8,
                "Philipp Lackner",
                "Build mobile apps with JavaScript/TypeScript and React skills. Master Expo file-based routing, native hardware integration (camera, push notifications), and Tailwind styling with NativeWind.",
                99.99
            ),
            new Course(
                "AI-Powered Content Marketing & Copywriting",
                "Leverage ChatGPT, Midjourney, and Jasper to scale growth marketing.",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop&q=60",
                "Digital Marketing",
                "12 hours",
                4.7,
                "Ryan Deiss",
                "Transform digital marketing workflows with generative AI tools. Learn prompt techniques for high-converting sales copy, social media automation workflows, and AI graphic asset creation.",
                49.99
            )
        );
        User clara = userRepository.findByEmail("clara@nebula.com").orElse(null);
        String claraId = clara != null ? clara.getId() : null;
        for (Course c : courses) {
            c.setInstructorId(claraId);
        }
        courseRepository.saveAll(courses);
    }
}
