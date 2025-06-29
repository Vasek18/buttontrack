# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Accept build args for React env vars
ARG REACT_APP_GOOGLE_CLIENT_ID
ARG REACT_APP_API_URL

# Pass them into the environment so React can access via process.env
ENV REACT_APP_GOOGLE_CLIENT_ID=$REACT_APP_GOOGLE_CLIENT_ID
ENV REACT_APP_API_URL=$REACT_APP_API_URL

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the app with injected env variables
RUN npm run build

# Serve stage
FROM node:18-alpine

WORKDIR /app

# Install serve to host static files
RUN npm install -g serve

# Copy built app
COPY --from=build /app/build ./build

# Expose port
EXPOSE 3000

# Serve the app
CMD ["serve", "-s", "build", "-l", "3000"]
