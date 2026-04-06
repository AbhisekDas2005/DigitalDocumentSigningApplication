# Digital Document Signing Application

A complete desktop application built in standard Java (AWT + Swing) to seamlessly view, digitally stamp, and cryptographically sign PDF and TXT documents securely.

## Features
- **Native PDF Rendering:** Uses Apache PDFBox to natively render documents with perfect scaling and shadow effects.
- **Dual Signature Methods:**
  - **Draw:** A real-time Java2D canvas allowing users to hand-draw signatures using their mouse/trackpad.
  - **Upload:** Intelligently processes image uploads (PNG/JPG), automatically stripping white backgrounds and cropping whitespace to seamlessly overlay onto documents.
- **Interactive Stamping:** Ghost-preview of the signature follows the mouse, allowing precision clicking to place the signature exactly where desired.
- **Direct Save (Overwrite):** Instantly modifies the source document with the signature stamped precisely in place with no resolution loss.
- **Cryptographic Engine:** (Behind the scenes) Uses RSA-2048 keypairs and SHA-256 for secure document signing.

## Tech Stack
- **Language:** Java 17+
- **GUI Framework:** Java Swing, Abstract Window Toolkit (AWT), Java2D API
- **Document Processing:** Apache PDFBox 3.0.1
- **Build Tool:** Apache Maven

## How to Run

### Prerequisites
1. Ensure Java 17+ (JDK) is installed.
2. Ensure Maven is installed (`mvn -version`).

### Build and Execute
Run the following commands in the root directory:
```bash
# 1. Compile the project and build the FAT Jar
mvn clean package

# 2. Run the executable jar
java -jar target/digital-signer-1.0.jar
```

## Team Members
- Aditya Gupta
- Alkesh Jaat
- Praanjal Garg
- Piyush Aggarwal
- Abhisek Das
