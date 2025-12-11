# CmdZen

A lightweight CLI tool that helps users with Linux commands through AI-powered recommendations, error analysis, and learning opportunities.

## 🚀 Features

- **Natural Language to Commands**: Convert plain English descriptions into Linux commands
- **Command Explanation**: Understand what commands do in simple terms
- **Error Analysis**: Get explanations and fixes for command errors
- **Linux Distribution Support**: Get help specific to your distribution
- **Customization**: Create command aliases and personalize your experience

## 📋 Requirements

- Java 21 or higher
- Linux-based operating system

## 🛠️ Installation

### From Source

```bash
# Clone the repository
git clone https://github.com/yourusername/cmdzen.git
cd cmdzen

# Build the project
./gradlew build

# Run the application
./gradlew run
```


## 🚀 Quick Start

### Basic Usage

```bash
# Start CmdZen
cmdzen

# Get help
cmdzen help

# Check version
cmdzen version
```

### Shell Integration (Recommended)

Install shell integration for automatic error capture:

```bash
# Install shell integration
cmdzen integrate

# Reload your shell
source ~/.bashrc  # or ~/.zshrc, etc.

# Verify installation
cmdzen integration-status
```

### Using the Solve Command

```bash
# Run any command that fails
ls /nonexistent

# Get AI-powered help instantly
cmdzen solve

# Or add context
cmdzen solve "I'm trying to list a directory"
```

See [SHELL_INTEGRATION.md](SHELL_INTEGRATION.md) for detailed documentation.
## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

