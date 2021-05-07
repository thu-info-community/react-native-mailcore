require 'json'
version = JSON.parse(File.read('package.json'))["version"]

Pod::Spec.new do |s|

  s.name           = "THUInfoMailcore"
  s.version        = version
  s.summary        = "react native bindings for https://github.com/MailCore/mailcore2, based on react-native-mailcore, customized for thu info"
  s.homepage       = "https://github.com/thu-info-community/thu-info-mailcore"
  s.license        = "MIT"
  s.author         = { "UNIDY2002" => "UNIDY2002@outlook.com" }
  s.platforms      = { :ios => "10.0" }
  s.source         = { :git => "https://github.com/thu-info-community/thu-info-mailcore.git" }
  s.source_files   = 'ios/**/*.{h,m}'
  s.dependency 'React-Core'
  s.dependency 'mailcore2-ios'

end
