# Student Data Manager

## Getting Started (Local Setup)

### Prerequisites

- Java 17
- Gradle
- Docker (for local DynamoDB)
- AWS CLI (for DynamoDB setup)
- Node.js & npm (for React chatbot UI)

---

## Local Installation and Running Steps (with Chatbot Integration)

### 1. **Start Local DynamoDB (using Docker)**
```bash
docker run -d -p 8000:8000 --name dynamodb-local amazon/dynamodb-local
```

### 2. **Create DynamoDB Table**
```bash
aws dynamodb create-table --table-name student-data --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 --endpoint-url http://localhost:8000
```
--0
### 3. **Set Gemini API Key**
- Obtain a free Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
- Add this to `src/main/resources/application.properties`:
  ```
  gemini.api.key=YOUR_GEMINI_API_KEY
  ```

### 4. **Configure DynamoDB Endpoint in Spring Boot**
- In your `src/main/resources/application.properties`, ensure you have:
  ```
  amazon.dynamodb.endpoint=http://localhost:8000
  amazon.aws.accesskey=dummy
  amazon.aws.secretkey=dummy
  amazon.aws.region=us-west-2
  ```

### 5. **Build and Run the Backend**
```bash
./gradlew bootRun
```
- The backend will be accessible at `http://localhost:8080`.

### 6. **Run the React Chatbot UI**

#### a. **Navigate to the chatbot UI directory**
```bash
cd chatbot-ui
```

#### b. **Install dependencies**
```bash
npm install
```

#### c. **Start the React development server**
```bash
npm start
```
- The chatbot UI will be available at [http://localhost:3000](http://localhost:3000)

---

## How the Chatbot Integration Works
- The React chatbot UI allows users to enter natural language queries.
- The UI sends the prompt to the backend endpoint (`/v1/chatbot/query`).
- The backend uses the Gemini API to interpret the prompt, generate a DynamoDB query/filter, fetches the data, and (optionally) uses Gemini again to summarize the results.
- The backend returns a user-friendly response, which is displayed in the chat UI.

