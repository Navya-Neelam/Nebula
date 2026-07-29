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
  readonly itemsPerPage = 3;

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
  }

  fetchCourses() {
    this.isLoading.set(true);
    this.courseService.getCourses().subscribe({
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

    // Mock payment gateway delay
    setTimeout(() => {
      this.enrolledCourseTitles.update(titles => [...titles, course.title]);
      this.showToast(`🎉 Purchase Successful! Enrolled in "${course.title}".`);
      this.closeCheckout();
      this.closeCourseDetails();
    }, 2000);
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
