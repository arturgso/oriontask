FROM oven/bun:1.1.38 AS build
WORKDIR /app

ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}

COPY package.json package-lock.json ./
RUN bun install

COPY . .
RUN bun run build

FROM oven/bun:1.1.38-alpine
WORKDIR /app

ENV NODE_ENV=production
ENV PATH="/root/.bun/bin:${PATH}"

COPY --from=build /app/dist ./dist
EXPOSE 3000

CMD ["bunx", "--bun", "serve", "-s", "dist", "-l", "3000"]
