# Add Flutter SDK to your app.

To integrate the adjoe Flutter SDK into your Flutter project, follow these steps:

1. Open your project's `pubspec.yaml` file.

2. Add the adjoe SDK as a dependency under the `dependencies` section:

```yaml
dependencies:
  adjoe:
    git:
      url: https://github.com/adjoeio/adjoe-flutter-sdk
      ref: main
```
3. save the `pubspec.yaml` and run a `pub get` command on your Editor/IDE or run the following command:
```
flutter pub get
```
