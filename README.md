# 🌍 ResQNet – Disaster Relief Management System

ResQNet is a **full-stack disaster management platform** designed to connect **communities in crisis** with the **right responders, resources, and administrators**.  
It enables disaster reporting, resource request tracking, contribution management, and real-time notifications to ensure relief efforts are **faster, smarter, and safer**.

---

##  Key Features

###  Backend (Spring Boot)
- **JWT Authentication & Role-based Access**
  - Roles: `REPORTER`, `RESPONDER`, `ADMIN`.
  - Secure APIs with Spring Security + BCrypt password hashing.
- **Domain Models**
  - `User`, `Disaster`, `ResourceRequest`, `Contribution`, `Notification`.
- **Business Logic Services**
  - Handles disaster reporting, request creation, contribution tracking, and notification dispatch.
- **Event-Driven Notifications**
  - Apache **Kafka** publishes and consumes system notifications asynchronously.
- **Admin Dashboard**
  - User, Disaster, Request, Contribution, Notification management.
  - Summary statistics for system monitoring.
- **Validation & Exception Handling**
  - Input validation via Jakarta.
  - Global exception handler with consistent error responses.
- **Database**
  - JPA/Hibernate entities mapped to PostgreSQL (configurable).

---

###  Frontend (React + TailwindCSS)
- **Authentication**
  - Login & Register (Reporter/Responder).
  - Role-based routing with `ProtectedRoute`.
- **Dashboards**
  - **Reporter** → Manage disasters, requests, and contributions.  
  - **Responder** → View all open requests and manage own contributions.  
  - **Admin** → Full system control with dashboard, maps, charts, and CRUD operations.
- **Interactive Maps (Leaflet)**
  - Disaster markers with category/status icons.
  - Reporter & Responder views for geolocation-based interaction.
- **Data Visualization**
  - Admin Summary with **Chart.js** (Pie charts with datalabels).
- **Tables with Filters & Pagination**
  - Disasters, Requests, Contributions, Users, Notifications.
- **Notifications**
  - View, mark as read, delete.
  - Auto-refresh every 60 seconds.
- **Modern UI**
  - Tailwind CSS gradients, custom animations, responsive layouts.

---

## 🛠 Tech Stack

### **Backend**
- **Framework**: Spring Boot 3.x  
- **Security**: Spring Security, JWT, BCrypt  
- **Database**: PostgreSQL (JPA/Hibernate ORM)  
- **Messaging**: Apache Kafka, Spring Kafka  
- **Validation**: Jakarta Validation API  
- **Build Tool**: Maven  
- **Language**: Java 17+  

### **Frontend**
- **Framework**: React 18  
- **Styling**: Tailwind CSS, custom animations  
- **Routing**: React Router v6  
- **State/Auth**: React Context API (`AuthContext`)  
- **Charts**: Chart.js + chartjs-plugin-datalabels  
- **Maps**: Leaflet + react-leaflet  
- **Icons**: React Icons (Feather)  
- **Notifications**: react-hot-toast  
- **HTTP Client**: Axios (with interceptors for JWT)  
- **Build Tool**: Vite  

---

##  System Architecture

```plaintext
Frontend (React + Tailwind) ↔ Backend (Spring Boot REST APIs) ↔ PostgreSQL
                                   │
                                   └── Apache Kafka (async notifications)
```

- **Reporter** → Reports disasters, raises resource requests.  
- **Responder** → Views requests, contributes resources.  
- **Admin** → Manages users, disasters, requests, contributions, and monitors system health.  
- **Notifications** → Triggered on every action (disaster, request, contribution). Published via Kafka, consumed and persisted, then fetched by users.

---

##  Installation & Setup

###  Backend Setup
```bash
# Clone repo
git clone https://github.com/your-repo/resqnet.git
cd resqnet/backend

# Configure PostgreSQL (application.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/resqnet
spring.datasource.username=postgres
spring.datasource.password=yourpassword

# Start Kafka/Zookeeper locally (example with Docker)
docker-compose up -d

# Run backend
mvn spring-boot:run
```

Backend will run on **http://localhost:8080/api**

---

###  Frontend Setup
```bash
cd ../frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend will run on **http://localhost:5173**

---

## 📡 API Endpoints (High-Level)

### Auth
- `POST /api/auth/register` → Register Reporter/Responder.
- `POST /api/auth/login` → Login, returns JWT.

### Disasters
- `POST /api/disasters` (Reporter).
- `GET /api/disasters` (All).
- `PUT/DELETE /api/disasters/{id}` (Admin).

### Requests
- `POST /api/requests` (Reporter).
- `GET /api/requests` (All).
- `PUT/DELETE /api/requests/{id}` (Admin).

### Contributions
- `POST /api/contributions` (Responder).
- `GET /api/contributions/responder/{email}`.
- `DELETE /api/contributions/{id}` (Admin).

### Notifications
- `GET /api/notifications` (User-specific).
- `PUT /api/notifications/{id}/read`.
- `DELETE /api/notifications/{id}`.

### Admin
- `GET /api/admin/summary` → Dashboard counts.
- CRUD: Users, Disasters, Requests, Contributions, Notifications.

---

## 📑 Frontend Pages

### Reporter
- `MyDisasters` – View & filter own disasters.  
- `MyRequests` – Track own requests (pending, partial, fulfilled).  
- `Contributions` – See contributions responders made to reporter’s requests.  
- `Dashboard (ReporterMapView)` – Map-based disaster/request management.  

### Responder
- `AllRequests` – Browse all open requests, filter by category/status.  
- `MyContributions` – View own contributions.  
- `Dashboard (ResponderMapView)` – Map-based contribution system.  

### Admin
- `Dashboard` – Quick access to all management features.  
- `Map Dashboard` – Map with disaster, request, and contribution visualization.  
- `Manage Users` – View/delete users.  
- `Manage Disasters` – View/filter/delete disasters.  
- `Manage Requests` – View/filter/delete requests.  
- `Manage Contributions` – View/filter/delete contributions.  
- `Manage Notifications` – Admin broadcast and system notifications.  
- `Summary` – Charts showing request statuses, user role breakdown.  

### Shared
- `Login`, `Register`, `Welcome`.  
- `Notifications` – User-specific feed (mark read/delete).  
- `Footer` (always visible).  

---

## 🔮 Future Enhancements
- Real-time notifications via WebSockets (instead of polling).  
- Disaster/request/contribution **edit forms** (not just delete).  
- Bulk export (CSV/Excel) for admin reports.  
- Clustering markers on the map for scalability.  
- Password reset & email verification.  
- Deployment-ready Docker images for both frontend & backend.  

---

## 👥 Authors
- **Akshay Keerthi Adhikasavan Suresh** – Full-stack developer, architect, and designer of ResQNet.  
- Built with using **Java Spring Boot + React + Tailwind + Kafka + PostgreSQL**.  
