import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { CourseService, Course } from '../../services/course.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  private courseService = inject(CourseService);

  // States using Signals
  courses = signal<Course[]>([]);
  selectedCategory = signal<string>('All');
  searchQuery = signal<string>('');
  currentPage = signal<number>(0);
  selectedCourse = signal<Course | null>(null);
  isLoading = signal<boolean>(true);
  toastMessage = signal<string | null>(null);
  imageErrors = signal<{ [key: string]: boolean }>({});

  // Checkout and Enrollment States
  checkoutCourse = signal<Course | null>(null);
  isPaying = signal<boolean>(false);
  enrolledCourseTitles = signal<string[]>([]);

  // Card input bindings (for checkout modal visual inputs)
  cardNumber = '';
  cardExpiry = '';
  cardCvv = '';

  // Hardcoded fallback courses with highly cached, stable Unsplash image IDs and prices
  readonly fallbackCourses: Course[] = [
    {
      title: "Introduction to Generative AI",
      description: "Learn the fundamentals of Generative AI, Large Language Models (LLMs), and prompt engineering.",
      imageUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=60",
      category: "Generative AI",
      duration: "6 hours",
      rating: 4.8,
      instructor: "Dr. Clara Sterling",
      detailDescription: "This course provides a comprehensive introduction to Generative AI. You will learn the history of transformer models, prompt engineering patterns, fine-tuning techniques, and how to build applications using LLM APIs. Ideal for beginners and professionals wanting to stay ahead in tech.",
      price: 49.99
    },
    {
      title: "Advanced Prompt BootCamp Engineering",
      description: "Master complex prompt engineering frameworks and build multi-agent LLM systems.",
      imageUrl: "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?w=800&auto=format&fit=crop&q=60",
      category: "Generative AI",
      duration: "8 hours",
      rating: 4.9,
      instructor: "Alex Rivera",
      detailDescription: "Unlock the true potential of AI with advanced techniques like Chain-of-Thought, ReAct framing, and Directional Stimulus prompting. You will construct autonomous agents using LangChain and learn practical security measures against prompt injection.",
      price: 79.99
    },
    {
      title: "Data Science & Machine Learning Bootcamp",
      description: "Go from zero to hero in Python, Pandas, NumPy, Scikit-Learn, and statistics.",
      imageUrl: "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=60",
      category: "Data Science",
      duration: "40 hours",
      rating: 4.7,
      instructor: "Prof. Marcus Vance",
      detailDescription: "A complete bootcamp covering the full data science lifecycle. Learn data visualization, exploratory analysis, linear regression, decision trees, random forests, and unsupervised learning algorithms. Includes hands-on projects with real-world datasets.",
      price: 149.99
    },
    {
      title: "Deep Learning & Neural Networks",
      description: "Understand CNNs, RNNs, and Transformers using PyTorch and TensorFlow.",
      imageUrl: "https://images.unsplash.com/photo-1527474305487-b87b222841cc?w=800&auto=format&fit=crop&q=60",
      category: "Data Science",
      duration: "25 hours",
      rating: 4.8,
      instructor: "Dr. Sophia Chen",
      detailDescription: "Delve deep into neural network architectures. Learn backpropagation, activation functions, convolutional layers for computer vision, recurrent structures for sequence processing, and the revolutionary attention mechanism that powers today's LLMs.",
      price: 119.99
    },
    {
      title: "Introduction to SQL & Database Design",
      description: "Master PostgreSQL, relational database architecture, and complex subqueries.",
      imageUrl: "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&auto=format&fit=crop&q=60",
      category: "Data Science",
      duration: "14 hours",
      rating: 4.7,
      instructor: "Colt Steele",
      detailDescription: "Learn relational design principles, indexing optimizations, and query writing. Write complex inner/outer joins, aggregate analytical operations, subqueries, trigger statements, and window mathematical functions.",
      price: 39.99
    },
    {
      title: "Angular 18 Complete Guide",
      description: "Master standalone components, signals, routing, and RxJS in modern Angular.",
      imageUrl: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "30 hours",
      rating: 4.8,
      instructor: "Maximilian Schwarz",
      detailDescription: "Build performant enterprise web applications using modern Angular. Learn the new Signals state management model, standalone component architectures, advanced routing strategies, and complex RxJS data streams.",
      price: 99.99
    },
    {
      title: "Full-Stack Spring Boot & React",
      description: "Connect Spring Boot REST APIs with a modern React frontend using Spring Security.",
      imageUrl: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "35 hours",
      rating: 4.9,
      instructor: "John Thompson",
      detailDescription: "Develop a complete production-ready application. Build secure Spring Boot REST endpoints with JWT validation, connect to a PostgreSQL database, and manage front-end authentication states seamlessly using React Context and Axios.",
      price: 129.99
    },
    {
      title: "Figma UI/UX Design Essentials",
      description: "Learn user research, wireframing, prototyping, and design systems in Figma.",
      imageUrl: "https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "20 hours",
      rating: 4.8,
      instructor: "Dan Walter",
      detailDescription: "Design high-fidelity interactive user interfaces and build robust user experience maps. Create nested layout components, configure responsive grids, run micro-interactive prototyping flows, and share design handoffs with developers.",
      price: 59.99
    },
    {
      title: "Modern JavaScript & TypeScript Masterclass",
      description: "Master ES6+, asynchronous programming, closures, and TypeScript compiler basics.",
      imageUrl: "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "22 hours",
      rating: 4.8,
      instructor: "Sarah Drasner",
      detailDescription: "Become a JavaScript expert. Learn advanced concepts like closures, async-await architectures, execution contexts, variable scopes, event loops, and type-safety modeling using TypeScript. Highly recommended for all developers.",
      price: 69.99
    },
    {
      title: "Spring Boot Microservices Masterclass",
      description: "Build scalable backend systems using Spring Cloud, Eureka, Gateway, and Config Server.",
      imageUrl: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "28 hours",
      rating: 4.9,
      instructor: "Dr. John Doe",
      detailDescription: "Master microservices architecture using Spring Boot and Spring Cloud. Learn service discovery with Eureka, API Gateway routing, centralized configuration, circuit breakers with Resilience4j, and event-driven communication using Kafka.",
      price: 119.99
    },
    {
      title: "Node.js Advanced API Design",
      description: "Learn scalable Express APIs, MongoDB, Redis caching, and WebSocket integrations.",
      imageUrl: "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "24 hours",
      rating: 4.8,
      instructor: "Sarah Connor",
      detailDescription: "Go deep into Node.js. Build high-performance REST and GraphQL APIs, implement JWT authentication, secure endpoints with rate limiting, cache queries with Redis, and implement real-time communication using WebSockets.",
      price: 89.99
    },
    {
      title: "Go (Golang) Backend Essentials",
      description: "Build highly concurrent microservices with Fiber, Gin, GORM, and Docker.",
      imageUrl: "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "20 hours",
      rating: 4.7,
      instructor: "Ken Thompson",
      detailDescription: "Learn Google's Go language for backend systems. Harness Go routines and channels for high concurrency, write clean APIs with the Fiber framework, interface with databases using GORM, and write unit and integration tests.",
      price: 99.99
    },
    {
      title: "Python Django & FastAPI Bootcamp",
      description: "Build reactive backends with Django ORM, REST Framework, and asynchronous FastAPI.",
      imageUrl: "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "32 hours",
      rating: 4.8,
      instructor: "Guido van Rossum",
      detailDescription: "A comprehensive backend bootcamp using Python. Build relational CRUD systems using Django ORM and Django REST Framework, and develop blazingly fast asynchronous APIs using FastAPI and Pydantic validation.",
      price: 109.99
    },
    {
      title: "Docker & Kubernetes for Developers",
      description: "Learn containerization, scaling, orchestration, and local microservice deployment.",
      imageUrl: "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
      category: "Cloud Computing",
      duration: "15 hours",
      rating: 4.6,
      instructor: "Nigel Poulton",
      detailDescription: "Understand how to package applications into lightweight containers, manage multi-container systems using Docker Compose, and orchestrate container lifecycles in scale using Kubernetes clusters and Helm charts.",
      price: 59.99
    },
    {
      title: "AWS Certified Solutions Architect",
      description: "Pass the SAA-C03 exam and learn AWS EC2, S3, RDS, Lambda, and VPC inside out.",
      imageUrl: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=60",
      category: "Cloud Computing",
      duration: "28 hours",
      rating: 4.8,
      instructor: "Stephane Maarek",
      detailDescription: "Fully prepare for the AWS Solutions Architect Associate exam. Learn cloud architecture best practices, compute hosting options, scalable database configurations, serverless execution models, and secure private virtual networking.",
      price: 129.99
    },
    {
      title: "Cybersecurity Fundamentals",
      description: "Learn cryptography, network defense, threat hunting, and social engineering.",
      imageUrl: "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
      category: "Cybersecurity",
      duration: "18 hours",
      rating: 4.7,
      instructor: "Sarah Jenkins",
      detailDescription: "Start your journey in ethical hacking and defense. Understand public-key cryptography, firewalls, intrusion detection systems, malware analysis tools, and the human psychological triggers exploited by social engineering campaigns.",
      price: 69.99
    },
    {
      title: "Ethical Hacking: Web Penetration Testing",
      description: "Discover SQL injection, XSS, CSRF, and session hijacking using Kali Linux.",
      imageUrl: "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
      category: "Cybersecurity",
      duration: "22 hours",
      rating: 4.9,
      instructor: "Daniel Lowrie",
      detailDescription: "Perform comprehensive web app security audits. Learn to identify OWASP Top 10 vulnerabilities like SQL injection, cross-site scripting (XSS), cross-site request forgery (CSRF), and insecure direct object references using professional pentesting tools.",
      price: 89.99
    },
    {
      title: "iOS 18 Swift & SwiftUI Bootcamp",
      description: "Build beautiful iOS apps using Swift, SwiftUI, SwiftData, and CoreAnimation.",
      imageUrl: "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800&auto=format&fit=crop&q=60",
      category: "Mobile Development",
      duration: "32 hours",
      rating: 4.8,
      instructor: "Angela Yu",
      detailDescription: "Master Apple's modern declarative UI framework. Build complex user interfaces, save persistent local data with SwiftData, integrate native maps, configure notification systems, and prepare your apps for the App Store.",
      price: 99.99
    },
    {
      title: "Android 15 Jetpack Compose Guide",
      description: "Learn Kotlin, Jetpack Compose UI, Clean Architecture, and Coroutines.",
      imageUrl: "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=800&auto=format&fit=crop&q=60",
      category: "Mobile Development",
      duration: "28 hours",
      rating: 4.7,
      instructor: "Philipp Lackner",
      detailDescription: "Build native Android applications with Kotlin and modern reactive UI tools. Explore MVVM/MVI architecture, state flows, reactive Compose layouts, work manager, local database access via Room, and test-driven development integrations.",
      price: 89.99
    },
    {
      title: "Digital Marketing Masters Class",
      description: "Master Google Ads, SEO, content strategy, email marketing, and analytics.",
      imageUrl: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop&q=60",
      category: "Digital Marketing",
      duration: "12 hours",
      rating: 4.6,
      instructor: "Ryan Deiss",
      detailDescription: "A comprehensive masterclass on driving web traffic and converting leads. Learn to rank websites using search engine optimization (SEO), construct effective search ad campaigns, write email copy, and read analytics data dashboards.",
      price: 39.99
    },
    {
      title: "SEO Strategy & Copywriting",
      description: "Learn keyword research, on-page optimization, and high-converting writing.",
      imageUrl: "https://images.unsplash.com/photo-1432888622747-4eb9a8efeb07?w=800&auto=format&fit=crop&q=60",
      category: "Digital Marketing",
      duration: "10 hours",
      rating: 4.5,
      instructor: "Brian Dean",
      detailDescription: "Boost organic search engine rankings and visitor retention. Learn to perform search intent keyword research, write titles that get clicks, structure layout headings, and build natural backlink relationships.",
      price: 29.99
    },
    {
      title: "Autonomous AI Agents & Multi-Agent Frameworks",
      description: "Build autonomous agent networks with AutoGen, CrewAI, and LangGraph.",
      imageUrl: "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=800&auto=format&fit=crop&q=60",
      category: "Generative AI",
      duration: "14 hours",
      rating: 4.9,
      instructor: "Alex Rivera",
      detailDescription: "Architect state-of-the-art multi-agent AI applications. Learn how autonomous agents plan tasks, tool usage, memory integration, and agent orchestration with LangGraph and CrewAI.",
      price: 89.99
    },
    {
      title: "LLM Fine-Tuning & Quantization Masterclass",
      description: "Learn LoRA, QLoRA, HuggingFace Transformers, and vLLM deployment.",
      imageUrl: "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=800&auto=format&fit=crop&q=60",
      category: "Generative AI",
      duration: "18 hours",
      rating: 4.9,
      instructor: "Dr. Clara Sterling",
      detailDescription: "Fine-tune open-source models like Llama 3 and Mistral on domain datasets using Parameter-Efficient Fine-Tuning (PEFT), LoRA, and QLoRA. Deploy quantized models with vLLM for low-latency inferencing.",
      price: 109.99
    },
    {
      title: "RAG Engineering & Vector Databases",
      description: "Build production Retrieval-Augmented Generation systems with Pinecone, Qdrant, and LlamaIndex.",
      imageUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=60",
      category: "Generative AI",
      duration: "12 hours",
      rating: 4.8,
      instructor: "Dr. Clara Sterling",
      detailDescription: "Master RAG architectures. Learn document chunking strategies, embedding models, vector similarity indexing with Pinecone and Qdrant, hybrid search, re-ranking, and evaluate RAG accuracy using Ragas.",
      price: 79.99
    },
    {
      title: "Multimodal AI & Vision Language Models",
      description: "Harness GPT-4 Vision, Claude 3, and Whisper for multimodal AI products.",
      imageUrl: "https://images.unsplash.com/photo-1507146426996-ef05306b995a?w=800&auto=format&fit=crop&q=60",
      category: "Generative AI",
      duration: "16 hours",
      rating: 4.8,
      instructor: "Sophia Chen",
      detailDescription: "Build applications that see, hear, and converse. Integrate vision language models, automatic speech recognition with OpenAI Whisper, and generative image/video synthesis APIs.",
      price: 94.99
    },
    {
      title: "Data Engineering & Analytics with Snowflake & dbt",
      description: "Build modern cloud data warehouses and ETL pipelines with dbt.",
      imageUrl: "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=60",
      category: "Data Science",
      duration: "22 hours",
      rating: 4.8,
      instructor: "Prof. Marcus Vance",
      detailDescription: "Master modern data stack engineering. Design dimensional data models in Snowflake, write SQL transformations using dbt, orchestrate data runs with Airflow, and enforce data quality testing.",
      price: 119.99
    },
    {
      title: "MLOps & ML Infrastructure with MLflow & Kubeflow",
      description: "Deploy, monitor, and scale machine learning models in production.",
      imageUrl: "https://images.unsplash.com/photo-1527474305487-b87b222841cc?w=800&auto=format&fit=crop&q=60",
      category: "Data Science",
      duration: "26 hours",
      rating: 4.9,
      instructor: "Dr. Sophia Chen",
      detailDescription: "Bridge the gap between data science and DevOps. Implement experiment tracking with MLflow, model registries, CI/CD pipelines for ML models, feature stores, and drift monitoring.",
      price: 129.99
    },
    {
      title: "Big Data Processing with Apache Spark & Databricks",
      description: "Process terabytes of streaming and batch data using PySpark.",
      imageUrl: "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&auto=format&fit=crop&q=60",
      category: "Data Science",
      duration: "30 hours",
      rating: 4.7,
      instructor: "Prof. Marcus Vance",
      detailDescription: "Learn large-scale distributed data processing. Write PySpark batch transformations, structured streaming queries, Delta Lake lakehouse architectures, and run distributed ML algorithms on Databricks clusters.",
      price: 139.99
    },
    {
      title: "Next.js 14 & React 19 Full-Stack Architecture",
      description: "Master Server Components, Server Actions, App Router, and Prisma.",
      imageUrl: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "28 hours",
      rating: 4.9,
      instructor: "Maximilian Schwarz",
      detailDescription: "Build modern, lightning-fast full-stack web apps using Next.js 14 App Router. Learn React Server Components, Server Actions for seamless mutations, Tailwind styling, Auth.js, and Prisma ORM.",
      price: 109.99
    },
    {
      title: "Vue 3 & Nuxt 3 Full-Stack Enterprise Guide",
      description: "Build reactive web apps with Composition API, Pinia, and Nuxt 3.",
      imageUrl: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "20 hours",
      rating: 4.8,
      instructor: "Sarah Drasner",
      detailDescription: "Master Vue 3's script setup and Composition API. Build server-side rendered (SSR) web applications using Nuxt 3, manage global reactive state with Pinia, and deploy to Vercel.",
      price: 89.99
    },
    {
      title: "Microfrontends Architecture & Module Federation",
      description: "Architect scalable enterprise frontend systems using Webpack 5.",
      imageUrl: "https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "18 hours",
      rating: 4.7,
      instructor: "Dan Walter",
      detailDescription: "Decompose monolithic frontend applications into independently deployable microfrontends using Webpack 5 Module Federation, Single-SPA framework, and shared component design libraries.",
      price: 99.99
    },
    {
      title: "Tailwind CSS & Modern UI Animation Masterclass",
      description: "Create wowed interfaces with Tailwind, Framer Motion, and Three.js.",
      imageUrl: "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=800&auto=format&fit=crop&q=60",
      category: "Web Development",
      duration: "14 hours",
      rating: 4.8,
      instructor: "Sarah Drasner",
      detailDescription: "Craft futuristic, interactive web experiences. Learn Tailwind utility techniques, glassmorphism layouts, micro-interactions, layout transitions with Framer Motion, and 3D web graphics with Three.js.",
      price: 49.99
    },
    {
      title: "Rust Systems & Backend Engineering",
      description: "Build high-performance, memory-safe web services using Axum, Actix, and Tokio.",
      imageUrl: "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "26 hours",
      rating: 4.9,
      instructor: "Ken Thompson",
      detailDescription: "Master Rust's memory safety guarantees, ownership, and borrowing. Build blazingly fast asynchronous HTTP APIs using Axum and Tokio, interface with PostgreSQL via SQLx, and write zero-cost abstractions.",
      price: 119.99
    },
    {
      title: "GraphQL API Development with Node & Spring",
      description: "Design schema-first GraphQL APIs with Federation and Apollo Server.",
      imageUrl: "https://images.unsplash.com/photo-1542831371-29b0f74f9713?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "18 hours",
      rating: 4.8,
      instructor: "Sarah Connor",
      detailDescription: "Learn GraphQL schema definitions, query resolvers, mutations, subscriptions, N+1 query batching with DataLoader, and federated GraphQL microservice architectures.",
      price: 79.99
    },
    {
      title: "Event-Driven Microservices with Apache Kafka",
      description: "Build distributed streaming pipelines and saga pattern architecture.",
      imageUrl: "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "30 hours",
      rating: 4.9,
      instructor: "Dr. John Doe",
      detailDescription: "Architect resilient event-driven microservices. Master Kafka topics, partitions, consumer groups, Kafka Streams API, Schema Registry with Avro, and implement Saga pattern distributed transactions.",
      price: 129.99
    },
    {
      title: "Redis Caching & In-Memory Data Store Architecture",
      description: "Master Redis enterprise caching, Pub/Sub, and Rate Limiting.",
      imageUrl: "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800&auto=format&fit=crop&q=60",
      category: "Backend Development",
      duration: "12 hours",
      rating: 4.7,
      instructor: "Ken Thompson",
      detailDescription: "Optimize application performance with Redis. Implement cache-aside strategies, cache invalidation rules, distributed locking with Redlock, rate limiting, and real-time pub/sub channels.",
      price: 69.99
    },
    {
      title: "Terraform & Infrastructure as Code (IaC)",
      description: "Automate multi-cloud AWS, Azure, and GCP deployments using Terraform.",
      imageUrl: "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
      category: "Cloud Computing",
      duration: "20 hours",
      rating: 4.8,
      instructor: "Nigel Poulton",
      detailDescription: "Automate cloud provisioning. Write reusable Terraform modules, manage remote state in S3 with DynamoDB locking, manage secrets, and configure CI/CD infrastructure deployments.",
      price: 89.99
    },
    {
      title: "Microsoft Azure Solutions Architect Bootcamp",
      description: "Master Azure Virtual Machines, AKS, Blob Storage, and Entra ID.",
      imageUrl: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=60",
      category: "Cloud Computing",
      duration: "28 hours",
      rating: 4.8,
      instructor: "Stephane Maarek",
      detailDescription: "Pass the Azure Solutions Architect Expert exam (AZ-305). Design hybrid cloud compute solutions, configure Azure Kubernetes Service (AKS), manage Azure SQL databases, and enforce IAM via Entra ID.",
      price: 119.99
    },
    {
      title: "Google Cloud Platform (GCP) Professional Engineer",
      description: "Design scalable cloud infrastructure on GCP Compute Engine & GKE.",
      imageUrl: "https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?w=800&auto=format&fit=crop&q=60",
      category: "Cloud Computing",
      duration: "26 hours",
      rating: 4.8,
      instructor: "Stephane Maarek",
      detailDescription: "Master GCP core services. Build containerized cloud workloads using Google Kubernetes Engine (GKE), BigQuery data warehouses, Cloud Functions serverless execution, and VPC networking.",
      price: 129.99
    },
    {
      title: "Kubernetes Security & Hardening (CKS)",
      description: "Secure production Kubernetes clusters, RBAC, network policies, and Falco.",
      imageUrl: "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
      category: "Cloud Computing",
      duration: "22 hours",
      rating: 4.9,
      instructor: "Nigel Poulton",
      detailDescription: "Prepare for the Certified Kubernetes Security Specialist (CKS) exam. Learn cluster hardening, network policies, Pod security standards, container image vulnerability scanning, and runtime threat detection with Falco.",
      price: 99.99
    },
    {
      title: "Cloud Security & DevSecOps Engineering",
      description: "Integrate security into CI/CD pipelines, static analysis, and container scanning.",
      imageUrl: "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
      category: "Cybersecurity",
      duration: "20 hours",
      rating: 4.8,
      instructor: "Sarah Jenkins",
      detailDescription: "Automate security checks across software development lifecycles. Implement SAST with SonarQube, DAST scanning with OWASP ZAP, dependency audit tools, and container image signing.",
      price: 99.99
    },
    {
      title: "CompTIA Security+ (SY0-701) Certification",
      description: "Master network security, risk management, incident response, and SOC ops.",
      imageUrl: "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&auto=format&fit=crop&q=60",
      category: "Cybersecurity",
      duration: "24 hours",
      rating: 4.7,
      instructor: "Daniel Lowrie",
      detailDescription: "Comprehensive prep for the Security+ certification exam. Learn core cybersecurity principles, threat actor profiles, cryptographic protocols, security architecture, and SOC incident triage.",
      price: 79.99
    },
    {
      title: "API Hacking & OWASP API Security Top 10",
      description: "Test and secure REST, GraphQL, and gRPC APIs against zero-day exploits.",
      imageUrl: "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=60",
      category: "Cybersecurity",
      duration: "18 hours",
      rating: 4.9,
      instructor: "Daniel Lowrie",
      detailDescription: "Identify and fix API security flaws. Master hands-on penetration testing techniques for Broken Object Level Authorization (BOLA), mass assignment, rate limit bypasses, and JWT signature forgery.",
      price: 89.99
    },
    {
      title: "Flutter & Dart Cross-Platform Mobile Apps",
      description: "Build iOS, Android, and Web apps from a single codebase with Riverpod.",
      imageUrl: "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=800&auto=format&fit=crop&q=60",
      category: "Mobile Development",
      duration: "30 hours",
      rating: 4.8,
      instructor: "Angela Yu",
      detailDescription: "Master Google's Flutter framework. Build responsive widget trees, handle state management using Riverpod, integrate Firebase backend services, and publish apps to Apple App Store & Google Play.",
      price: 89.99
    },
    {
      title: "React Native & Expo Full-Stack Mastery",
      description: "Build cross-platform native mobile apps with Expo Router and NativeWind.",
      imageUrl: "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=800&auto=format&fit=crop&q=60",
      category: "Mobile Development",
      duration: "26 hours",
      rating: 4.8,
      instructor: "Philipp Lackner",
      detailDescription: "Build mobile apps with JavaScript/TypeScript and React skills. Master Expo file-based routing, native hardware integration (camera, push notifications), and Tailwind styling with NativeWind.",
      price: 99.99
    },
    {
      title: "AI-Powered Content Marketing & Copywriting",
      description: "Leverage ChatGPT, Midjourney, and Jasper to scale growth marketing.",
      imageUrl: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop&q=60",
      category: "Digital Marketing",
      duration: "12 hours",
      rating: 4.7,
      instructor: "Ryan Deiss",
      detailDescription: "Transform digital marketing workflows with generative AI tools. Learn prompt techniques for high-converting sales copy, social media automation workflows, and AI graphic asset creation.",
      price: 49.99
    }
  ];

  // Categories list (added Backend Development)
  readonly categories: string[] = [
    'All',
    'Generative AI',
    'Data Science',
    'Web Development',
    'Backend Development',
    'Cloud Computing',
    'Cybersecurity',
    'Mobile Development',
    'Digital Marketing'
  ];

  // Pagination constant
  readonly itemsPerPage = 6;

  // Computed properties
  filteredCourses = computed(() => {
    const category = this.selectedCategory();
    const query = this.searchQuery().toLowerCase().trim();
    let result = this.courses();

    if (category !== 'All') {
      result = result.filter(c => c.category === category);
    }

    if (query) {
      result = result.filter(c =>
        c.title.toLowerCase().includes(query) ||
        c.description.toLowerCase().includes(query) ||
        c.category.toLowerCase().includes(query) ||
        (c.instructor && c.instructor.toLowerCase().includes(query))
      );
    }

    return result;
  });

  totalPages = computed(() => {
    const count = this.filteredCourses().length;
    return Math.max(1, Math.ceil(count / this.itemsPerPage));
  });

  totalPagesArray = computed(() => {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  });

  visibleCourses = computed(() => {
    const start = this.currentPage() * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredCourses().slice(start, end);
  });

  ngOnInit() {
    this.fetchCourses();
    this.fetchEnrolledCourses();
  }

  fetchEnrolledCourses() {
    if (this.authService.isAuthenticated()) {
      this.courseService.getEnrolledCourses().subscribe({
        next: (enrolledList) => {
          const titles = enrolledList.map(c => c.title);
          this.enrolledCourseTitles.set(titles);
        },
        error: () => {}
      });
    }
  }

  fetchCourses() {
    this.isLoading.set(true);
    this.courseService.getCourses({ size: 100 }).subscribe({
      next: (res) => {
        const courseList = res?.courses || [];
        if (courseList && courseList.length > 0) {
          this.courses.set(courseList);
        } else {
          this.courses.set(this.fallbackCourses);
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load courses from API, loading local fallback:', err);
        this.courses.set(this.fallbackCourses);
        this.isLoading.set(false);
      }
    });
  }

  selectCategory(category: string) {
    this.selectedCategory.set(category);
    this.currentPage.set(0); // Reset page on category change
  }

  onSearchChange(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.searchQuery.set(value);
    this.currentPage.set(0); // Reset page on search change
  }

  nextPage() {
    const current = this.currentPage();
    const total = this.totalPages();
    if (current < total - 1) {
      this.currentPage.set(current + 1);
    } else {
      this.currentPage.set(0); // Loop back to start
    }
  }

  prevPage() {
    const current = this.currentPage();
    const total = this.totalPages();
    if (current > 0) {
      this.currentPage.set(current - 1);
    } else {
      this.currentPage.set(total - 1); // Loop back to end
    }
  }

  setPage(pageIndex: number) {
    this.currentPage.set(pageIndex);
  }

  openCourseDetails(course: Course) {
    this.selectedCourse.set(course);
  }

  closeCourseDetails() {
    this.selectedCourse.set(null);
  }

  isEnrolled(course: Course): boolean {
    return this.enrolledCourseTitles().includes(course.title);
  }

  buyCourse(course: Course) {
    this.checkoutCourse.set(course);
  }

  closeCheckout() {
    this.checkoutCourse.set(null);
    this.isPaying.set(false);
    this.cardNumber = '';
    this.cardExpiry = '';
    this.cardCvv = '';
  }

  confirmCheckout() {
    const course = this.checkoutCourse();
    if (!course) return;

    this.isPaying.set(true);

    if (course.id) {
      this.courseService.enrollInCourse(course.id).subscribe({
        next: () => {
          this.enrolledCourseTitles.update(titles => [...titles, course.title]);
          this.showToast(`🎉 Purchase Successful! Enrolled in "${course.title}".`);
          this.closeCheckout();
          this.closeCourseDetails();
        },
        error: (err) => {
          // If already enrolled or offline fallback
          this.enrolledCourseTitles.update(titles => [...titles, course.title]);
          this.showToast(`🎉 Enrolled in "${course.title}".`);
          this.closeCheckout();
          this.closeCourseDetails();
        }
      });
    } else {
      setTimeout(() => {
        this.enrolledCourseTitles.update(titles => [...titles, course.title]);
        this.showToast(`🎉 Purchase Successful! Enrolled in "${course.title}".`);
        this.closeCheckout();
        this.closeCourseDetails();
      }, 1500);
    }
  }

  addToCart(course: Course) {
    this.showToast(`🛒 "${course.title}" has been added to your cart!`);
    this.closeCourseDetails();
  }

  onImageError(title: string) {
    this.imageErrors.update(errs => ({ ...errs, [title]: true }));
  }

  getCategoryClass(category: string): string {
    switch (category) {
      case 'Generative AI': return 'gradient-ai';
      case 'Data Science': return 'gradient-ds';
      case 'Web Development': return 'gradient-web';
      case 'Backend Development': return 'gradient-ds';
      case 'Cloud Computing': return 'gradient-cloud';
      case 'Cybersecurity': return 'gradient-sec';
      case 'Mobile Development': return 'gradient-mob';
      case 'Digital Marketing': return 'gradient-mkt';
      default: return 'gradient-web';
    }
  }

  private showToast(message: string) {
    this.toastMessage.set(message);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 3500);
  }

  logout() {
    this.authService.logout();
  }
}
