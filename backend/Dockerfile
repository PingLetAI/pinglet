FROM node:20-alpine AS build
WORKDIR /app
RUN apk add --no-cache openssl
COPY package.json package-lock.json* pnpm-lock.yaml* yarn.lock* ./
RUN npm ci
COPY . .
RUN npm run prisma:generate
RUN npm run build

FROM node:20-alpine
WORKDIR /app
RUN apk add --no-cache openssl ffmpeg tesseract-ocr python3 py3-pip \
    && python3 -m pip install --no-cache-dir --break-system-packages yt-dlp
COPY --from=build /app /app
CMD ["node", "dist/main"]
